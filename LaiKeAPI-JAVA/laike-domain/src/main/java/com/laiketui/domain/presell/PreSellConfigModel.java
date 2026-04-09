package com.laiketui.domain.presell;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sunH_
 */
@Table(name = "lkt_pre_sell_config")
public class PreSellConfigModel implements Serializable
{
    /**
     * 主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer store_id;

    /**
     * 是否开启 1 是 0 否
     */
    private Integer is_open;

    /**
     * 定金预售说明
     */
    private String deposit_desc;

    /**
     * 订货预售说明
     */
    private String balance_desc;

    /**
     * 订单失效 (单位 秒)
     */
    private Integer order_failure;

    /**
     * 订单售后时间 (单位秒)
     */
    private Integer order_after;

    /**
     * 自动收货时间（单位秒）
     */
    private Integer auto_the_goods;

    /**
     * 提醒发货限制 间隔(单位秒)
     */
    private Integer deliver_remind;

    /**
     * 多件包邮设置 0.未开启 1.开启
     */
    private Integer package_settings;

    /**
     * 同件
     */
    private Integer same_piece;

    /**
     * 同单
     */
    private Integer same_order;

    /**
     * 自动评价设置几后自动好评
     */
    private Integer auto_good_comment_day;

    /**
     * 自动评价设置几后自动好评内容
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
     * 获取主键自增
     *
     * @return id - 主键自增
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键自增
     *
     * @param id 主键自增
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
     * 获取是否开启 1 是 0 否
     *
     * @return is_open - 是否开启 1 是 0 否
     */
    public Integer getIs_open()
    {
        return is_open;
    }

    /**
     * 设置是否开启 1 是 0 否
     *
     * @param is_open 是否开启 1 是 0 否
     */
    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }

    public String getDeposit_desc()
    {
        return deposit_desc;
    }

    public void setDeposit_desc(String deposit_desc)
    {
        this.deposit_desc = deposit_desc;
    }

    public String getBalance_desc()
    {
        return balance_desc;
    }

    public void setBalance_desc(String balance_desc)
    {
        this.balance_desc = balance_desc;
    }

    /**
     * 获取订单失效 (单位 秒)
     *
     * @return order_failure - 订单失效 (单位 秒)
     */
    public Integer getOrder_failure()
    {
        return order_failure;
    }

    /**
     * 设置订单失效 (单位 秒)
     *
     * @param order_failure 订单失效 (单位 秒)
     */
    public void setOrder_failure(Integer order_failure)
    {
        this.order_failure = order_failure;
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
     * 获取自动收货时间（单位秒）
     *
     * @return auto_the_goods - 自动收货时间（单位秒）
     */
    public Integer getAuto_the_goods()
    {
        return auto_the_goods;
    }

    /**
     * 设置自动收货时间（单位秒）
     *
     * @param auto_the_goods 自动收货时间（单位秒）
     */
    public void setAuto_the_goods(Integer auto_the_goods)
    {
        this.auto_the_goods = auto_the_goods;
    }

    /**
     * 获取提醒发货限制 间隔(单位秒)
     *
     * @return deliver_remind - 提醒发货限制 间隔(单位秒)
     */
    public Integer getDeliver_remind()
    {
        return deliver_remind;
    }

    /**
     * 设置提醒发货限制 间隔(单位秒)
     *
     * @param deliver_remind 提醒发货限制 间隔(单位秒)
     */
    public void setDeliver_remind(Integer deliver_remind)
    {
        this.deliver_remind = deliver_remind;
    }

    /**
     * 获取多件包邮设置 0.未开启 1.开启
     *
     * @return package_settings - 多件包邮设置 0.未开启 1.开启
     */
    public Integer getPackage_settings()
    {
        return package_settings;
    }

    /**
     * 设置多件包邮设置 0.未开启 1.开启
     *
     * @param package_settings 多件包邮设置 0.未开启 1.开启
     */
    public void setPackage_settings(Integer package_settings)
    {
        this.package_settings = package_settings;
    }

    /**
     * 获取同件
     *
     * @return same_piece - 同件
     */
    public Integer getSame_piece()
    {
        return same_piece;
    }

    /**
     * 设置同件
     *
     * @param same_piece 同件
     */
    public void setSame_piece(Integer same_piece)
    {
        this.same_piece = same_piece;
    }

    /**
     * 获取同单
     *
     * @return same_order - 同单
     */
    public Integer getSame_order()
    {
        return same_order;
    }

    /**
     * 设置同单
     *
     * @param same_order 同单
     */
    public void setSame_order(Integer same_order)
    {
        this.same_order = same_order;
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
}