package com.laiketui.domain.seckill;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀标签
 *
 * @author Trick
 * @date 2021/10/14 14:08
 */
@Table(name = "lkt_seconds_label")
public class SecondsLabelModel implements Serializable
{
    /**
     * 展示标签
     */
    public static final Integer IS_SHOW_OK = 1;

    /**
     * 不展示标签
     */
    public static final Integer IS_SHOW_NO = 0;


    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private Integer store_id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签副标题
     */
    private String title;

    private Integer is_show;

    /**
     * 序号
     */
    private Integer sort;

    /**
     * 添加时间
     */
    private Date add_date;

    private Date update_date;

    /**
     * 店铺id
     */
    @Column(name = "mch_id")
    private Integer mhc_id;

    /**
     * 回收标识
     */
    private Integer recovery;

    public Integer getRecovery()
    {
        return recovery;
    }

    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }

    public Integer getMhc_id()
    {
        return mhc_id;
    }

    public void setMhc_id(Integer mhc_id)
    {
        this.mhc_id = mhc_id;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
    }

    /**
     * @return store_id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * @param store_id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取标签名称
     *
     * @return name - 标签名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置标签名称
     *
     * @param name 标签名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取标签副标题
     *
     * @return title - 标签副标题
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * 设置标签副标题
     *
     * @param title 标签副标题
     */
    public void setTitle(String title)
    {
        this.title = title == null ? null : title.trim();
    }

    /**
     * @return is_show
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * @param is_show
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取序号
     *
     * @return sort - 序号
     */
    public Integer getSort()
    {
        return sort;
    }

    /**
     * 设置序号
     *
     * @param sort 序号
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
     * @return update_date
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * @param update_date
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }
}