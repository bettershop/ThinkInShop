package com.laiketui.domain.group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼团商品表
 *
 * @author Trick
 * @date 2023/3/20 16:16
 */
@Table(name = "lkt_group_goods")
public class


GroupGoodsModel implements Serializable
{
    /**
     * 活动商品id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 活动id
     */
    private String activity_id;

    /**
     * 商品id
     */
    private Integer goods_id;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 删除标识
     */
    private Integer recycle;

    /**
     * 获取活动商品id
     *
     * @return id - 活动商品id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置活动商品id
     *
     * @param id 活动商品id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取活动id
     *
     * @return activity_id - 活动id
     */
    public String getActivity_id()
    {
        return activity_id;
    }

    /**
     * 设置活动id
     *
     * @param activity_id 活动id
     */
    public void setActivity_id(String activity_id)
    {
        this.activity_id = activity_id == null ? null : activity_id.trim();
    }

    /**
     * 获取商品id
     *
     * @return goods_id - 商品id
     */
    public Integer getGoods_id()
    {
        return goods_id;
    }

    /**
     * 设置商品id
     *
     * @param goods_id 商品id
     */
    public void setGoods_id(Integer goods_id)
    {
        this.goods_id = goods_id;
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
     * 获取删除标识
     *
     * @return recycle - 删除标识
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置删除标识
     *
     * @param recycle 删除标识
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }
}