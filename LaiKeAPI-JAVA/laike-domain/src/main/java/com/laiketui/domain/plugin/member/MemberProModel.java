package com.laiketui.domain.plugin.member;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_member_pro")
public class MemberProModel implements Serializable
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
     * 商品id
     */
    private Integer pro_id;

    /**
     * 是否回收 0.否 1是
     */
    private Integer recovery;

    /**
     * 添加时间
     */
    private Date add_time;

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
     * 获取商品id
     *
     * @return pro_id - 商品id
     */
    public Integer getPro_id()
    {
        return pro_id;
    }

    /**
     * 设置商品id
     *
     * @param pro_id 商品id
     */
    public void setPro_id(Integer pro_id)
    {
        this.pro_id = pro_id;
    }

    /**
     * 获取是否回收 0.否 1是
     *
     * @return recovery - 是否回收 0.否 1是
     */
    public Integer getRecovery()
    {
        return recovery;
    }

    /**
     * 设置是否回收 0.否 1是
     *
     * @param recovery 是否回收 0.否 1是
     */
    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }
}