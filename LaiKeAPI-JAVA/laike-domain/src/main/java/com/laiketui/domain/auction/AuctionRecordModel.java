package com.laiketui.domain.auction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞拍记录表
 *
 * @author Trick
 * @date 2022/7/4 16:56
 */
@Table(name = "lkt_auction_record")
public class AuctionRecordModel implements Serializable
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
     * 竞拍场次id
     */
    private Integer auction_id;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 竞拍价格
     */
    private BigDecimal price;

    /**
     * 用户id
     */
    private String user_id;

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
     * 获取竞拍商品id
     *
     * @return auction_id - 竞拍商品id
     */
    public Integer getAuction_id()
    {
        return auction_id;
    }

    /**
     * 设置竞拍商品id
     *
     * @param auction_id 竞拍商品id
     */
    public void setAuction_id(Integer auction_id)
    {
        this.auction_id = auction_id;
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

    /**
     * 获取竞拍价格
     *
     * @return price - 竞拍价格
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 设置竞拍价格
     *
     * @param price 竞拍价格
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /**
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户id
     *
     * @param user_id 用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }
}