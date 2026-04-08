package com.laiketui.domain.plugin.seckill;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_seconds_record")
public class SecondsRecordModel implements Serializable
{
    /**
     * id自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 活动id
     */
    private Integer activity_id;

    /**
     * 时段id
     */
    private Integer time_id;

    /**
     * 商品id
     */
    private Integer pro_id;

    /**
     * 规格id
     */
    private Integer attr_id;

    /**
     * 秒杀id
     */
    private Integer sec_id;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 秒杀订单
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 是否删除 1是 0否
     */
    private Integer is_delete;

    /**
     * 添加日期
     */
    private Date add_time;

    /**
     * 获取id自增
     *
     * @return id - id自增
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id自增
     *
     * @param id id自增
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取店铺id
     *
     * @return store_id - 店铺id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置店铺id
     *
     * @param store_id 店铺id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
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

    /**
     * 获取活动id
     *
     * @return activity_id - 活动id
     */
    public Integer getActivity_id()
    {
        return activity_id;
    }

    /**
     * 设置活动id
     *
     * @param activity_id 活动id
     */
    public void setActivity_id(Integer activity_id)
    {
        this.activity_id = activity_id;
    }

    /**
     * 获取时段id
     *
     * @return time_id - 时段id
     */
    public Integer getTime_id()
    {
        return time_id;
    }

    /**
     * 设置时段id
     *
     * @param time_id 时段id
     */
    public void setTime_id(Integer time_id)
    {
        this.time_id = time_id;
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
     * 获取秒杀id
     *
     * @return sec_id - 秒杀id
     */
    public Integer getSec_id()
    {
        return sec_id;
    }

    /**
     * 设置秒杀id
     *
     * @param sec_id 秒杀id
     */
    public void setSec_id(Integer sec_id)
    {
        this.sec_id = sec_id;
    }

    /**
     * 获取价格
     *
     * @return price - 价格
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 设置价格
     *
     * @param price 价格
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /**
     * 获取数量
     *
     * @return num - 数量
     */
    public Integer getNum()
    {
        return num;
    }

    /**
     * 设置数量
     *
     * @param num 数量
     */
    public void setNum(Integer num)
    {
        this.num = num;
    }

    /**
     * 获取秒杀订单
     *
     * @return sNo - 秒杀订单
     */
    public String getsNo()
    {
        return sNo;
    }

    /**
     * 设置秒杀订单
     *
     * @param sNo 秒杀订单
     */
    public void setsNo(String sNo)
    {
        this.sNo = sNo == null ? null : sNo.trim();
    }

    /**
     * 获取是否删除 1是 0否
     *
     * @return is_delete - 是否删除 1是 0否
     */
    public Integer getIs_delete()
    {
        return is_delete;
    }

    /**
     * 设置是否删除 1是 0否
     *
     * @param is_delete 是否删除 1是 0否
     */
    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }

    /**
     * 获取添加日期
     *
     * @return add_time - 添加日期
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加日期
     *
     * @param add_time 添加日期
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }
}