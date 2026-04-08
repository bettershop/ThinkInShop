package com.laiketui.domain.group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼团订单设置
 *
 * @author Trick
 * @date 2023/4/4 18:12
 */
@Table(name = "lkt_group_order_config")
public class GroupOrderConfigModel implements Serializable
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
     * 订单售后时间 (单位秒)
     */
    private Integer order_after;

    /**
     * 自动收货时间
     */
    private Integer auto_the_goods;

    /**
     * 自动评价设置几后自动好评
     */
    private Integer auto_good_comment_day;

    /**
     * 自动好评内容
     */
    private String auto_good_comment_content;

    public String getAuto_good_comment_content()
    {
        return auto_good_comment_content;
    }

    public void setAuto_good_comment_content(String auto_good_comment_content)
    {
        this.auto_good_comment_content = auto_good_comment_content;
    }

    /**
     * 创建时间
     */
    private Date create_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 规则
     */
    private String content;

    /**
     * 是否开启拼团插件 插件状态 0关闭 1开启
     */
    private Integer isOpen;

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

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
     * 获取订单售后时间 (单位秒)
     *
     * @return order_after - 订单售后时间 (单位秒)
     */
    public Integer getOrder_after()
    {
        return order_after;
    }

    /**
     * 设置订单售后时间 (单位秒)
     *
     * @param order_after 订单售后时间 (单位秒)
     */
    public void setOrder_after(Integer order_after)
    {
        this.order_after = order_after;
    }

    /**
     * 获取自动收货时间
     *
     * @return auto_the_goods - 自动收货时间
     */
    public Integer getAuto_the_goods()
    {
        return auto_the_goods;
    }

    /**
     * 设置自动收货时间
     *
     * @param auto_the_goods 自动收货时间
     */
    public void setAuto_the_goods(Integer auto_the_goods)
    {
        this.auto_the_goods = auto_the_goods;
    }

    /**
     * 获取自动评价设置几后自动好评
     *
     * @return auto_good_comment_day - 自动评价设置几后自动好评
     */
    public Integer getAuto_good_comment_day()
    {
        return auto_good_comment_day;
    }

    /**
     * 设置自动评价设置几后自动好评
     *
     * @param auto_good_comment_day 自动评价设置几后自动好评
     */
    public void setAuto_good_comment_day(Integer auto_good_comment_day)
    {
        this.auto_good_comment_day = auto_good_comment_day;
    }

    /**
     * 获取创建时间
     *
     * @return create_date - 创建时间
     */
    public Date getCreate_date()
    {
        return create_date;
    }

    /**
     * 设置创建时间
     *
     * @param create_date 创建时间
     */
    public void setCreate_date(Date create_date)
    {
        this.create_date = create_date;
    }

    /**
     * 获取修改时间
     *
     * @return update_date - 修改时间
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * 设置修改时间
     *
     * @param update_date 修改时间
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }
}