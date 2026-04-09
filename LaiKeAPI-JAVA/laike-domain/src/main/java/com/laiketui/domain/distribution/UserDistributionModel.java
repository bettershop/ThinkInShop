package com.laiketui.domain.distribution;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_user_distribution")
public class UserDistributionModel implements Serializable
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
     * 用户ID
     */
    private String user_id;

    /**
     * 上级id
     */
    private String pid;

    /**
     * 等级
     */
    private Integer level;

    /**
     * lt
     */
    @Column(name = "`lt`")
    @Transient
    private Integer lt;

    /**
     * rt
     */
    @Column(name = "`rt`")
    @Transient
    private Integer rt;

    /**
     * 第几代
     */
    private Integer uplevel;

    /**
     * 添加时间
     */
    private Date add_date;

    private String usets;

    /**
     * 个人佣金
     */
    private BigDecimal commission;

    /**
     * 累计消费
     */
    private BigDecimal onlyamount;

    /**
     * 销售业绩
     */
    private BigDecimal allamount;

    /**
     * 个人进货奖励已经发放最大条件
     */
    private BigDecimal one_put;

    /**
     * 团队业绩奖励已经发放最大
     */
    private BigDecimal team_put;

    /**
     * 累计佣金
     */
    private BigDecimal accumulative;

    /**
     * 可提现佣金
     */
    private BigDecimal tx_commission;

    public BigDecimal getAccumulative()
    {
        return accumulative;
    }

    public void setAccumulative(BigDecimal accumulative)
    {
        this.accumulative = accumulative;
    }

    public BigDecimal getTx_commission()
    {
        return tx_commission;
    }

    public void setTx_commission(BigDecimal tx_commission)
    {
        this.tx_commission = tx_commission;
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
     * 获取上级id
     *
     * @return pid - 上级id
     */
    public String getPid()
    {
        return pid;
    }

    /**
     * 设置上级id
     *
     * @param pid 上级id
     */
    public void setPid(String pid)
    {
        this.pid = pid == null ? null : pid.trim();
    }

    /**
     * 获取等级
     *
     * @return level - 等级
     */
    public Integer getLevel()
    {
        return level;
    }

    /**
     * 设置等级
     *
     * @param level 等级
     */
    public void setLevel(Integer level)
    {
        this.level = level;
    }

    /**
     * 获取lt
     *
     * @return lt - lt
     */
    public Integer getLt()
    {
        return lt;
    }

    /**
     * 设置lt
     *
     * @param lt lt
     */
    public void setLt(Integer lt)
    {
        this.lt = lt;
    }

    /**
     * 获取rt
     *
     * @return rt - rt
     */
    public Integer getRt()
    {
        return rt;
    }

    /**
     * 设置rt
     *
     * @param rt rt
     */
    public void setRt(Integer rt)
    {
        this.rt = rt;
    }

    /**
     * 获取第几代
     *
     * @return uplevel - 第几代
     */
    public Integer getUplevel()
    {
        return uplevel;
    }

    /**
     * 设置第几代
     *
     * @param uplevel 第几代
     */
    public void setUplevel(Integer uplevel)
    {
        this.uplevel = uplevel;
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
     * @return usets
     */
    public String getUsets()
    {
        return usets;
    }

    /**
     * @param usets
     */
    public void setUsets(String usets)
    {
        this.usets = usets == null ? null : usets.trim();
    }

    /**
     * 获取累计佣金
     *
     * @return commission - 累计佣金
     */
    public BigDecimal getCommission()
    {
        return commission;
    }

    /**
     * 设置累计佣金
     *
     * @param commission 累计佣金
     */
    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    /**
     * 获取累计消费
     *
     * @return onlyamount - 累计消费
     */
    public BigDecimal getOnlyamount()
    {
        return onlyamount;
    }

    /**
     * 设置累计消费
     *
     * @param onlyamount 累计消费
     */
    public void setOnlyamount(BigDecimal onlyamount)
    {
        this.onlyamount = onlyamount;
    }

    /**
     * 获取销售业绩
     *
     * @return allamount - 销售业绩
     */
    public BigDecimal getAllamount()
    {
        return allamount;
    }

    /**
     * 设置销售业绩
     *
     * @param allamount 销售业绩
     */
    public void setAllamount(BigDecimal allamount)
    {
        this.allamount = allamount;
    }

    /**
     * 获取个人进货奖励已经发放最大条件
     *
     * @return one_put - 个人进货奖励已经发放最大条件
     */
    public BigDecimal getOne_put()
    {
        return one_put;
    }

    /**
     * 设置个人进货奖励已经发放最大条件
     *
     * @param one_put 个人进货奖励已经发放最大条件
     */
    public void setOne_put(BigDecimal one_put)
    {
        this.one_put = one_put;
    }

    /**
     * 获取团队业绩奖励已经发放最大
     *
     * @return team_put - 团队业绩奖励已经发放最大
     */
    public BigDecimal getTeam_put()
    {
        return team_put;
    }

    /**
     * 设置团队业绩奖励已经发放最大
     *
     * @param team_put 团队业绩奖励已经发放最大
     */
    public void setTeam_put(BigDecimal team_put)
    {
        this.team_put = team_put;
    }
}