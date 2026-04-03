package com.laiketui.domain.distribution;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 佣金日志表
 *
 * @author Trick
 * @date 2021/2/7 10:29
 */
@Table(name = "lkt_distribution_record")
public class DistributionRecordModel implements Serializable
{

    /**
     * 日志类型
     * 1:转入(收入) 2:提现 3:个人进获奖 8:充值积分
     */
    public interface Type
    {
        /**
         * 转入
         */
        Integer SHIFT_TO          = 1;
        /**
         * 提现
         */
        Integer WITHDRAWAL        = 2;
        /**
         * 个人进获奖8
         */
        Integer ONLY_AWARD        = 3;
        /**
         * 充值积分
         */
        Integer RECHARGE_INTEGRAL = 8;
    }

    /**
     * 分佣类型 1 直推分佣 2 间推分佣 3 平级奖 4 级差奖
     */
    public interface CommissionType
    {
        /**
         * 直推分佣
         */
        Integer DIRECT_PUSH  = 1;
        /**
         * 间推分佣
         */
        Integer INTERPULSION = 2;
        /**
         * 平级奖
         */
        Integer SIDEWAYS     = 3;
        /**
         * 级差奖
         */
        Integer DIFFERENTIAL = 4;
    }

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
     * 购买员
     */
    private String user_id;

    /**
     * 分销员
     */
    private String from_id;

    /**
     * 佣金金额
     */
    private BigDecimal money;

    /**
     * 订单号
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 事件
     */
    private String event;

    /**
     * 类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
     */
    private Integer type;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 是否发放
     */
    private Integer status;
    /**
     * 用户当前分销等级
     */
    private Integer userLevel;

    public Integer getUserLevel()
    {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel)
    {
        this.userLevel = userLevel;
    }

    /**
     * 分佣类型 1直推分佣2间推分佣3平级奖4级差奖
     */
    private Integer genre;

    public Integer getGenre()
    {
        return genre;
    }

    public void setGenre(Integer genre)
    {
        this.genre = genre;
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
     * 获取购买员
     *
     * @return user_id - 购买员
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置购买员
     *
     * @param user_id 购买员
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取分销员
     *
     * @return from_id - 分销员
     */
    public String getFrom_id()
    {
        return from_id;
    }

    /**
     * 设置分销员
     *
     * @param from_id 分销员
     */
    public void setFrom_id(String from_id)
    {
        this.from_id = from_id == null ? null : from_id.trim();
    }

    /**
     * 获取佣金金额
     *
     * @return money - 佣金金额
     */
    public BigDecimal getMoney()
    {
        return money;
    }

    /**
     * 设置佣金金额
     *
     * @param money 佣金金额
     */
    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    /**
     * 获取订单号
     *
     * @return sNo - 订单号
     */
    public String getsNo()
    {
        return sNo;
    }

    /**
     * 设置订单号
     *
     * @param sNo 订单号
     */
    public void setsNo(String sNo)
    {
        this.sNo = sNo == null ? null : sNo.trim();
    }

    /**
     * 获取层级
     *
     * @return level - 层级
     */
    public Integer getLevel()
    {
        return level;
    }

    /**
     * 设置层级
     *
     * @param level 层级
     */
    public void setLevel(Integer level)
    {
        this.level = level;
    }

    /**
     * 获取事件
     *
     * @return event - 事件
     */
    public String getEvent()
    {
        return event;
    }

    /**
     * 设置事件
     *
     * @param event 事件
     */
    public void setEvent(String event)
    {
        this.event = event == null ? null : event.trim();
    }

    /**
     * 获取类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
     *
     * @return type - 类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
     *
     * @param type 类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
     */
    public void setType(Integer type)
    {
        this.type = type;
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
     * 获取是否发放
     *
     * @return status - 是否发放
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置是否发放
     *
     * @param status 是否发放
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }
}