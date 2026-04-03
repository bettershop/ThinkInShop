package com.laiketui.domain.plugin;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 分佣排行表
 *
 * @author Trick
 * @date 2021/2/8 13:57
 */
@Table(name = "lkt_distribution_ranking")
public class DistributionRankingModel implements Serializable
{
    /**
     * 当天排行
     */
    public static final int TOP_TODAY      = 1;
    /**
     * 本周排行
     */
    public static final int TOP_THIS_WEEK  = 2;
    /**
     * 本月排行
     */
    public static final int TOP_THIS_MONTH = 3;
    /**
     * 本年排行
     */
    public static final int TOP_THIS_YEAR  = 4;

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 用户ID
     */
    private String user_id;

    /**
     * 用户账号
     */
    private String zhanghao;

    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 排行类型 1今日2本周3本月4本年
     */
    private Integer type;

    /**
     * 获取主键ID
     *
     * @return id - 主键ID
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城ID
     *
     * @return store_id - 商城ID
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城ID
     *
     * @param store_id 商城ID
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取用户ID
     *
     * @return user_id - 用户ID
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户ID
     *
     * @param user_id 用户ID
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取用户账号
     *
     * @return zhanghao - 用户账号
     */
    public String getZhanghao()
    {
        return zhanghao;
    }

    /**
     * 设置用户账号
     *
     * @param zhanghao 用户账号
     */
    public void setZhanghao(String zhanghao)
    {
        this.zhanghao = zhanghao == null ? null : zhanghao.trim();
    }

    /**
     * 获取佣金
     *
     * @return commission - 佣金
     */
    public BigDecimal getCommission()
    {
        return commission;
    }

    /**
     * 设置佣金
     *
     * @param commission 佣金
     */
    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
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
     * 获取排行类型 1今日2本周3本月4本年
     *
     * @return type - 排行类型 1今日2本周3本月4本年
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置排行类型 1今日2本周3本月4本年
     *
     * @param type 排行类型 1今日2本周3本月4本年
     */
    public void setType(Integer type)
    {
        this.type = type;
    }
}