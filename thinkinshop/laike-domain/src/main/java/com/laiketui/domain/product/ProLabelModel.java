package com.laiketui.domain.product;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品标签
 *
 * @author Trick
 * @date 2021/6/25 18:11
 */
@Table(name = "lkt_pro_label")
public class ProLabelModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 商品标签
     */
    private String name;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 颜色 编码
     */
    private String color;

    /**
     * 语种
     */
    private String lang_code;

    /**
     * 所属国家
     */
    @Column(name = "country_num")
    private Integer country_num;

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
     * 获取商城ID
     *
     * @return store_id - 商城ID
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城ID
     *
     * @param store_id 商城ID
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取商品标签
     *
     * @return name - 商品标签
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置商品标签
     *
     * @param name 商品标签
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
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

    public String getLang_code()
    {
        return lang_code;
    }

    public void setLang_code(String lang_code)
    {
        this.lang_code = lang_code;
    }

    public Integer getCountry_num()
    {
        return country_num;
    }

    public void setCountry_num(Integer country_num)
    {
        this.country_num = country_num;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }
}