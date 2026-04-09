package com.laiketui.domain.plugin.bbs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author: liuao
 * @Date: 2025-09-28-14:32
 * @Description:
 */
@Data
@ApiModel(description = "分类表")
@Table(name = "lkt_bbs_category")
public class BbsCategoryModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "商城ID")
    private Integer store_id;

    @ApiModelProperty(value = "上级ID")
    private Long sid;

    @ApiModelProperty(value = "分类名称")
    private String name;

    @ApiModelProperty(value = "选中分类图片")
    private String img;

    @ApiModelProperty(value = "未选中分类图片")
    private String bimg;

    @ApiModelProperty(value = "等级")
    private Integer level;

    @ApiModelProperty(value = "排序")
    private Integer sort_order;

    @ApiModelProperty(value = "是否删除 0.未删除 1.删除")
    private Integer recycle;

    @ApiModelProperty(value = "创建时间")
    private Date create_time;

    @ApiModelProperty(value = "是否是热门 0.不是热门 1.热门")
    private Integer is_popular;

}
