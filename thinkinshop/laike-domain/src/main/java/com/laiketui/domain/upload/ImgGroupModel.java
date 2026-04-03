package com.laiketui.domain.upload;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 图片分组表
 *
 * @author Trick
 * @date 2021/7/7 18:34
 */
@Table(name = "lkt_img_group")
public class ImgGroupModel implements Serializable
{
    /**
     * 分组id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 0不是默认 1默认
     */
    private Integer is_default;

    /**
     * 图片1  视频2
     */
    private Integer type;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 是否为常驻显示
     */
    private Integer is_show;

    /**
     * 供应商id
     */
    private Integer supplier_id;

    /**
     * 获取分组id
     *
     * @return id - 分组id
     */
    public Integer getId()
    {
        return id;
    }

    public Integer getType()
    {
        return type;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getIs_show()
    {
        return is_show;
    }

    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    public Integer getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 设置分组id
     *
     * @param id 分组id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取分组名称
     *
     * @return name - 分组名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置分组名称
     *
     * @param name 分组名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取排序
     *
     * @return sort - 排序
     */
    public Integer getSort()
    {
        return sort;
    }

    /**
     * 设置排序
     *
     * @param sort 排序
     */
    public void setSort(Integer sort)
    {
        this.sort = sort;
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

    /**
     * 获取0不是默认 1默认
     *
     * @return is_default - 0不是默认 1默认
     */
    public Integer getIs_default()
    {
        return is_default;
    }

    /**
     * 设置0不是默认 1默认
     *
     * @param is_default 0不是默认 1默认
     */
    public void setIs_default(Integer is_default)
    {
        this.is_default = is_default;
    }
}