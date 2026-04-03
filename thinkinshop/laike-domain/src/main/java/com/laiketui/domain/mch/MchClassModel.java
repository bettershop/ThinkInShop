package com.laiketui.domain.mch;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_mch_class")
public class MchClassModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 名称
     */
    private String name;

    /**
     * 图片
     */
    private String img;

    /**
     * 排序值
     */
    private Integer sort;


    /**
     * 回收站 0:否 1:是
     */
    private Integer is_display;

    /**
     * 回收站 0:正常 1:回收
     */
    private Integer recycle;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 获取id
     *
     * @return id - id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取名称
     *
     * @return name - 名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取图片
     *
     * @return img - 图片
     */
    public String getImg()
    {
        return img;
    }

    /**
     * 设置图片
     *
     * @param img 图片
     */
    public void setImg(String img)
    {
        this.img = img == null ? null : img.trim();
    }

    /**
     * 获取排序值
     *
     * @return sort - 排序值
     */
    public Integer getSort()
    {
        return sort;
    }

    /**
     * 设置排序值
     *
     * @param sort 排序值
     */
    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Integer getIs_display()
    {
        return is_display;
    }

    public void setIs_display(Integer is_display)
    {
        this.is_display = is_display;
    }

    /**
     * 获取回收站 0:正常 1:回收
     *
     * @return recycle - 回收站 0:正常 1:回收
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收站 0:正常 1:回收
     *
     * @param recycle 回收站 0:正常 1:回收
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    /**
     * 获取添加时间
     *
     * @return add_date - 添加时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置添加时间
     *
     * @param add_date 添加时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }
}