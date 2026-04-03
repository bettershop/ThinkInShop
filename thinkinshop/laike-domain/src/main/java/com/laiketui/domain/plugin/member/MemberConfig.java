package com.laiketui.domain.plugin.member;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_member_config")
public class MemberConfig
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
     * 会员插件开关 0.关 1.开
     */
    private Integer is_open;

    /**
     * 开通设置
     */
    private String open_config;

    /**
     * 会员生日特权开关 0.关 1.开
     */
    private Integer birthday_open;

    /**
     * 积分倍数
     */
    private Integer points_multiple;

    /**
     * 会员赠送积分开关 0.关 1.开
     */
    private Integer bonus_points_open;

    /**
     * 会员赠送积分设置
     */
    private String bonus_points_config;

    /**
     * 会员打折率
     */
    private BigDecimal member_discount;

    /**
     * 续费提醒开关 0.关 1.开
     */
    private Integer renew_open;

    /**
     * 开始续费提醒天数
     */
    private Integer renew_day;

    /**
     * 会员权益设置
     */
    private String member_equity;

    /**
     * 添加时间
     */
    private Date add_time;

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
     * 获取会员插件开关 0.关 1.开
     *
     * @return is_open - 会员插件开关 0.关 1.开
     */
    public Integer getIs_open()
    {
        return is_open;
    }

    /**
     * 设置会员插件开关 0.关 1.开
     *
     * @param is_open 会员插件开关 0.关 1.开
     */
    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }

    /**
     * 获取开通设置
     *
     * @return open_config - 开通设置
     */
    public String getOpen_config()
    {
        return open_config;
    }

    /**
     * 设置开通设置
     *
     * @param open_config 开通设置
     */
    public void setOpen_config(String open_config)
    {
        this.open_config = open_config == null ? null : open_config.trim();
    }

    /**
     * 获取会员生日特权开关 0.关 1.开
     *
     * @return birthday_open - 会员生日特权开关 0.关 1.开
     */
    public Integer getBirthday_open()
    {
        return birthday_open;
    }

    /**
     * 设置会员生日特权开关 0.关 1.开
     *
     * @param birthday_open 会员生日特权开关 0.关 1.开
     */
    public void setBirthday_open(Integer birthday_open)
    {
        this.birthday_open = birthday_open;
    }

    /**
     * 获取积分倍数
     *
     * @return points_multiple - 积分倍数
     */
    public Integer getPoints_multiple()
    {
        return points_multiple;
    }

    /**
     * 设置积分倍数
     *
     * @param points_multiple 积分倍数
     */
    public void setPoints_multiple(Integer points_multiple)
    {
        this.points_multiple = points_multiple;
    }

    /**
     * 获取会员赠送积分开关 0.关 1.开
     *
     * @return bonus_points_open - 会员赠送积分开关 0.关 1.开
     */
    public Integer getBonus_points_open()
    {
        return bonus_points_open;
    }

    /**
     * 设置会员赠送积分开关 0.关 1.开
     *
     * @param bonus_points_open 会员赠送积分开关 0.关 1.开
     */
    public void setBonus_points_open(Integer bonus_points_open)
    {
        this.bonus_points_open = bonus_points_open;
    }

    /**
     * 获取会员赠送积分设置
     *
     * @return bonus_points_config - 会员赠送积分设置
     */
    public String getBonus_points_config()
    {
        return bonus_points_config;
    }

    /**
     * 设置会员赠送积分设置
     *
     * @param bonus_points_config 会员赠送积分设置
     */
    public void setBonus_points_config(String bonus_points_config)
    {
        this.bonus_points_config = bonus_points_config == null ? null : bonus_points_config.trim();
    }

    /**
     * 获取会员打折率
     *
     * @return member_discount - 会员打折率
     */
    public BigDecimal getMember_discount()
    {
        return member_discount;
    }

    /**
     * 设置会员打折率
     *
     * @param member_discount 会员打折率
     */
    public void setMember_discount(BigDecimal member_discount)
    {
        this.member_discount = member_discount;
    }

    /**
     * 获取续费提醒开关 0.关 1.开
     *
     * @return renew_open - 续费提醒开关 0.关 1.开
     */
    public Integer getRenew_open()
    {
        return renew_open;
    }

    /**
     * 设置续费提醒开关 0.关 1.开
     *
     * @param renew_open 续费提醒开关 0.关 1.开
     */
    public void setRenew_open(Integer renew_open)
    {
        this.renew_open = renew_open;
    }

    /**
     * 获取开始续费提醒天数
     *
     * @return renew_day - 开始续费提醒天数
     */
    public Integer getRenew_day()
    {
        return renew_day;
    }

    /**
     * 设置开始续费提醒天数
     *
     * @param renew_day 开始续费提醒天数
     */
    public void setRenew_day(Integer renew_day)
    {
        this.renew_day = renew_day;
    }

    /**
     * 获取会员权益设置
     *
     * @return member_equity - 会员权益设置
     */
    public String getMember_equity()
    {
        return member_equity;
    }

    /**
     * 设置会员权益设置
     *
     * @param member_equity 会员权益设置
     */
    public void setMember_equity(String member_equity)
    {
        this.member_equity = member_equity == null ? null : member_equity.trim();
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
}