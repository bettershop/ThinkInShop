package com.laiketui.domain.config;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * PC商城底部栏图片配置
 * gp
 * 2023-08-24
 */
@Table(name = "lkt_pc_mall_bottom")
public class PcMallBottomModel implements Serializable
{

    /**
     * id
     */
    @Id
    private Integer id;

    /**
     * 商城id
     */
    private Integer storeId;

    /**
     * 图片
     */
    private String image;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subheading;

    /**
     * 添加时间
     */
    private Date addDate;

    /**
     * 修改时间
     */
    private Date updateDate;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Date getAddDate()
    {
        return addDate;
    }

    public void setAddDate(Date addDate)
    {
        this.addDate = addDate;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
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
