package com.laiketui.domain.vo.pc;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑PC商城底部栏图片配置
 * gp
 * 2023-08-24
 */
public class AddBottomInfoVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "图片id")
    private Integer id;

    @ApiModelProperty(value = "images", name = "图片地址")
    private String images;

    @ApiModelProperty(value = "sort", name = "排序")
    private Integer sort;

    @ApiModelProperty(value = "title", name = "标题")
    private String title;

    @ApiModelProperty(value = "subheading", name = "副标题")
    private String subheading;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getImages()
    {
        return images;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSubheading()
    {
        return subheading;
    }

    public void setSubheading(String subheading)
    {
        this.subheading = subheading;
    }
}
