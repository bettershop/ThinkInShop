package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 添加/编辑楼层
 *
 * @author Trick
 * @date 2023/2/15 11:05
 */
@ApiModel(description = "添加/编辑楼层")
public class BlockManageVo extends MainVo implements Serializable
{

    @ApiModelProperty(value = "楼层id", name = "id")
    private String  id;
    @ApiModelProperty(value = "楼层名称", name = "id")
    private String  name;
    @ApiModelProperty(value = "序号", name = "sort")
    private Integer sort;

    @ApiModelProperty(value = "关键字", name = "id")
    private List<String> keyword;

    @ApiModelProperty(value = "图片", name = "img")
    private String img;

    public String getImg()
    {
        return img;
    }

    public void setImg(String img)
    {
        this.img = img;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getKeyword()
    {
        return keyword;
    }

    public void setKeyword(List<String> keyword)
    {
        this.keyword = keyword;
    }
}
