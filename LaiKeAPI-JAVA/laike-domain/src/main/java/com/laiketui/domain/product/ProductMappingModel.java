package com.laiketui.domain.product;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品映射表
 *
 * @author Trick
 * @date 2023/2/15 10:56
 */
@Table(name = "lkt_block_product")
public class ProductMappingModel implements Serializable
{
    /**
     * 主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 外键,建议使用uuid
     */
    private String main_id;

    /**
     * 商品id
     */
    private Integer product_id;

    /**
     * 序号
     */
    private Integer sort;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 获取主键id
     *
     * @return id - 主键id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键id
     *
     * @param id 主键id
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
     * 获取外键,建议使用uuid
     *
     * @return main_id - 外键,建议使用uuid
     */
    public String getMain_id()
    {
        return main_id;
    }

    /**
     * 设置外键,建议使用uuid
     *
     * @param main_id 外键,建议使用uuid
     */
    public void setMain_id(String main_id)
    {
        this.main_id = main_id == null ? null : main_id.trim();
    }

    /**
     * 获取商品id
     *
     * @return product_id - 商品id
     */
    public Integer getProduct_id()
    {
        return product_id;
    }

    /**
     * 设置商品id
     *
     * @param product_id 商品id
     */
    public void setProduct_id(Integer product_id)
    {
        this.product_id = product_id;
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
}