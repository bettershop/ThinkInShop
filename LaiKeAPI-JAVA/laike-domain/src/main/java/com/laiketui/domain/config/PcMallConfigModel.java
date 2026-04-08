package com.laiketui.domain.config;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

/**
 * PC商城页面显示配置
 * gp
 * 2023-08-24
 */
@Table(name = "lkt_pc_mall_config")
public class PcMallConfigModel implements Serializable
{

    /**
     * 1浏览器标签
     */
    @Transient
    public static final int LNQBQ = 1;
    /**
     * 2 登录页配置
     */
    @Transient
    public static final int DLYPZ = 2;
    /**
     * 3 首页配置
     */
    @Transient
    public static final int SYPZ  = 3;

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
     * 配置类型 1浏览器标签 2 登录页配置 3 首页配置
     */
    private Integer type;

    /**
     * 配置值
     */
    private String value;

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

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
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
}
