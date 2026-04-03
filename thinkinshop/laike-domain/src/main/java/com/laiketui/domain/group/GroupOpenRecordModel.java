package com.laiketui.domain.group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 参团记录表
 *
 * @author Trick
 * @date 2023/3/23 18:25
 */
@Table(name = "lkt_group_open_record")
public class GroupOpenRecordModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 开团id
     */
    private String open_id;

    /**
     * 商品id
     */
    private Integer goods_id;

    /**
     * 规格id
     */
    private Integer attr_id;

    /**
     * 零售价
     */
    private BigDecimal attr_price;

    /**
     * 参团金额
     */
    private BigDecimal price;

    /**
     * 参团人
     */
    private String user_id;

    /**
     * 临时订单号
     */
    private String sno;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 删除标识
     */
    private Integer recycle;

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
     * 获取开团id
     *
     * @return open_id - 开团id
     */
    public String getOpen_id()
    {
        return open_id;
    }

    /**
     * 设置开团id
     *
     * @param open_id 开团id
     */
    public void setOpen_id(String open_id)
    {
        this.open_id = open_id == null ? null : open_id.trim();
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
     * 获取规格id
     *
     * @return attr_id - 规格id
     */
    public Integer getAttr_id()
    {
        return attr_id;
    }

    /**
     * 设置规格id
     *
     * @param attr_id 规格id
     */
    public void setAttr_id(Integer attr_id)
    {
        this.attr_id = attr_id;
    }

    /**
     * 获取零售价
     *
     * @return attr_price - 零售价
     */
    public BigDecimal getAttr_price()
    {
        return attr_price;
    }

    /**
     * 设置零售价
     *
     * @param attr_price 零售价
     */
    public void setAttr_price(BigDecimal attr_price)
    {
        this.attr_price = attr_price;
    }

    /**
     * 获取参团金额
     *
     * @return price - 参团金额
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 设置参团金额
     *
     * @param price 参团金额
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /**
     * 获取参团人
     *
     * @return user_id - 参团人
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置参团人
     *
     * @param user_id 参团人
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取临时订单号
     *
     * @return sno - 临时订单号
     */
    public String getSno()
    {
        return sno;
    }

    /**
     * 设置临时订单号
     *
     * @param sno 临时订单号
     */
    public void setSno(String sno)
    {
        this.sno = sno == null ? null : sno.trim();
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