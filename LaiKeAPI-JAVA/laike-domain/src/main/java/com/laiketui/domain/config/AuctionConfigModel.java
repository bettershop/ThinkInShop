package com.laiketui.domain.config;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 竞拍配置表
 *
 * @author Trick
 * @date 2021/2/25 16:43
 */
@Table(name = "lkt_auction_config")
public class AuctionConfigModel implements Serializable
{
    /**
     * 主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 最低开拍人数
     */
    private Integer low_pepole;

    /**
     * 出价等待时间
     */
    private Integer wait_time;

    /**
     * 保留天数
     */
    private Integer days;

    /**
     * 竞拍规则
     */
    private String content;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 获取是否开启插件 0-不开启 1-开启
     */
    private Integer is_open;

    /**
     * 订单失效 (单位 秒)
     */
    private Long order_failure;

    /**
     * 自动收货时间
     */
    private Long auto_the_goods;

    /**
     * 发货提醒设置
     */
    private Long deliver_remind;

    /**
     * 竞拍协议
     */
    private String agree_content;
    /**
     * 协议标题
     */
    private String agree_title;

    public String getAgree_content()
    {
        return agree_content;
    }

    public void setAgree_content(String agree_content)
    {
        this.agree_content = agree_content;
    }

    public String getAgree_title()
    {
        return agree_title;
    }

    public void setAgree_title(String agree_title)
    {
        this.agree_title = agree_title;
    }

    public Long getOrder_failure()
    {
        return order_failure;
    }

    public void setOrder_failure(Long order_failure)
    {
        this.order_failure = order_failure;
    }

    public Long getAuto_the_goods()
    {
        return auto_the_goods;
    }

    public void setAuto_the_goods(Long auto_the_goods)
    {
        this.auto_the_goods = auto_the_goods;
    }

    public Long getDeliver_remind()
    {
        return deliver_remind;
    }

    public void setDeliver_remind(Long deliver_remind)
    {
        this.deliver_remind = deliver_remind;
    }

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
     * 获取最低开拍人数
     *
     * @return low_pepole - 最低开拍人数
     */
    public Integer getLow_pepole()
    {
        return low_pepole;
    }

    /**
     * 设置最低开拍人数
     *
     * @param low_pepole 最低开拍人数
     */
    public void setLow_pepole(Integer low_pepole)
    {
        this.low_pepole = low_pepole;
    }

    /**
     * 获取出价等待时间 /秒
     *
     * @return wait_time - 出价等待时间
     */
    public Integer getWait_time()
    {
        return wait_time;
    }

    /**
     * 设置出价等待时间 /秒
     *
     * @param wait_time 出价等待时间
     */
    public void setWait_time(Integer wait_time)
    {
        this.wait_time = wait_time;
    }

    /**
     * 获取保留天数
     *
     * @return days - 保留天数
     */
    public Integer getDays()
    {
        return days;
    }

    /**
     * 设置保留天数
     *
     * @param days 保留天数
     */
    public void setDays(Integer days)
    {
        this.days = days;
    }

    /**
     * 获取竞拍规则
     *
     * @return content - 竞拍规则
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置竞拍规则
     *
     * @param content 竞拍规则
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
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
     * 获取是否开启插件 0-不开启 1-开启
     *
     * @return is_open - 是否开启插件 0-不开启 1-开启
     */
    public Integer getIs_open()
    {
        return is_open;
    }

    /**
     * 设置是否开启插件 0-不开启 1-开启
     *
     * @param is_open 是否开启插件 0-不开启 1-开启
     */
    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }
}