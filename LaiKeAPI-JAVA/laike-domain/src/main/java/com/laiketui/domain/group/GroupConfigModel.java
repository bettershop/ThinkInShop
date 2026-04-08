package com.laiketui.domain.group;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼团配置
 *
 * @author Trick
 * @date 2023/3/20 16:39
 */
@Table(name = "lkt_group_config")
public class GroupConfigModel implements Serializable
{
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 退款方式 1,自动 2,手动
     */
    @Transient
    private Integer refunmoney;

    /**
     * 商城id
     */
    private Integer store_id;

    public Integer getIs_open()
    {
        return is_open;
    }

    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }

    /**
     * 是否开启拼团
     */
    @Transient
    private Integer is_open;

    /**
     * 时间段内的拼团商品将会展示在拼团抢先知
     */
    @Transient
    private Integer herald_time;

    /**
     * 规则
     */
    @Transient
    private String rule;

    public String getRule()
    {
        return rule;
    }

    public void setRule(String rule)
    {
        this.rule = rule;
    }

    public Integer getHerald_time()
    {
        return herald_time;
    }

    public void setHerald_time(Integer herald_time)
    {
        this.herald_time = herald_time;
    }

    public Integer getRefunmoney()
    {
        return refunmoney;
    }

    public void setRefunmoney(Integer refunmoney)
    {
        this.refunmoney = refunmoney;
    }

    public Integer getGroup_time()
    {
        return group_time;
    }

    public void setGroup_time(Integer group_time)
    {
        this.group_time = group_time;
    }

    /**
     * 拼团时间（拼团）
     */
    @Transient
    private Integer group_time;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 成团倒计时 0=不计时
     */
    private Integer end_time;

    /**
     * 开团限制数量 0=不限制
     */
    private Integer open_limit;

    /**
     * 参团限制数量 0=不限制
     */
    private Integer join_limit;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 规则说明
     */
    private String rule_content;

    /**
     * 开团数量
     */
    @Transient
    private Integer open_num;

    /**
     * 参团数量
     */
    @Transient
    private Integer can_num;

    public Integer getOpen_num()
    {
        return open_num;
    }

    public void setOpen_num(Integer open_num)
    {
        this.open_num = open_num;
    }

    public Integer getCan_num()
    {
        return can_num;
    }

    public void setCan_num(Integer can_num)
    {
        this.can_num = can_num;
    }

    public Integer getCan_again()
    {
        return can_again;
    }

    public void setCan_again(Integer can_again)
    {
        this.can_again = can_again;
    }

    public Integer getOpen_discount()
    {
        return open_discount;
    }

    public void setOpen_discount(Integer open_discount)
    {
        this.open_discount = open_discount;
    }

    /**
     * 是否可重复参团1 是 0 否
     */
    @Transient
    private Integer can_again;

    /**
     * 是否开启团长优惠 1 是 0 否
     */
    @Transient
    private Integer open_discount;

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取成团倒计时 0=不计时
     *
     * @return end_time - 成团倒计时 0=不计时
     */
    public Integer getEnd_time()
    {
        return end_time;
    }

    /**
     * 设置成团倒计时 0=不计时
     *
     * @param end_time 成团倒计时 0=不计时
     */
    public void setEnd_time(Integer end_time)
    {
        this.end_time = end_time;
    }

    /**
     * 获取开团限制数量 0=不限制
     *
     * @return open_limit - 开团限制数量 0=不限制
     */
    public Integer getOpen_limit()
    {
        return open_limit;
    }

    /**
     * 设置开团限制数量 0=不限制
     *
     * @param open_limit 开团限制数量 0=不限制
     */
    public void setOpen_limit(Integer open_limit)
    {
        this.open_limit = open_limit;
    }

    /**
     * 获取参团限制数量 0=不限制
     *
     * @return join_limit - 参团限制数量 0=不限制
     */
    public Integer getJoin_limit()
    {
        return join_limit;
    }

    /**
     * 设置参团限制数量 0=不限制
     *
     * @param join_limit 参团限制数量 0=不限制
     */
    public void setJoin_limit(Integer join_limit)
    {
        this.join_limit = join_limit;
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
     * 获取规则说明
     *
     * @return rule_content - 规则说明
     */
    public String getRule_content()
    {
        return rule_content;
    }

    /**
     * 设置规则说明
     *
     * @param rule_content 规则说明
     */
    public void setRule_content(String rule_content)
    {
        this.rule_content = rule_content == null ? null : rule_content.trim();
    }
}