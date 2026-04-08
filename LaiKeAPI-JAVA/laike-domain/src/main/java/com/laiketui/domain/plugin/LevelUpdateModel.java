package com.laiketui.domain.plugin;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 会员等级变更记录表
 *
 * @author Trick
 * @date 2021/2/6 14:09
 */
@Table(name = "lkt_level_update")
public class LevelUpdateModel implements Serializable
{

    /**
     * 规则
     */
    @Transient
    public static final int TYPE_RULE   = 1;
    /**
     * 礼包
     */
    @Transient
    public static final int TYPE_GIFT   = 2;
    /**
     * 系统
     */
    @Transient
    public static final int TYPE_SYSTEM = 3;

    /**
     * 记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户ID
     */
    private String user_id;

    /**
     * 途径 1规则 2礼包 3系统
     */
    private Integer type;

    /**
     * 变更前等级
     */
    private Integer old_level;

    /**
     * 变更后等级
     */
    private Integer up_level;

    /**
     * 变更时间
     */
    private Date add_time;

    /**
     * 获取记录ID
     *
     * @return id - 记录ID
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置记录ID
     *
     * @param id 记录ID
     */
    public void setId(Integer id)
    {
        this.id = id;
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
     * 获取途径 1规则 2礼包 3系统
     *
     * @return type - 途径 1规则 2礼包 3系统
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置途径 1规则 2礼包 3系统
     *
     * @param type 途径 1规则 2礼包 3系统
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取变更前等级
     *
     * @return old_level - 变更前等级
     */
    public Integer getOld_level()
    {
        return old_level;
    }

    /**
     * 设置变更前等级
     *
     * @param old_level 变更前等级
     */
    public void setOld_level(Integer old_level)
    {
        this.old_level = old_level;
    }

    /**
     * 获取变更后等级
     *
     * @return up_level - 变更后等级
     */
    public Integer getUp_level()
    {
        return up_level;
    }

    /**
     * 设置变更后等级
     *
     * @param up_level 变更后等级
     */
    public void setUp_level(Integer up_level)
    {
        this.up_level = up_level;
    }

    /**
     * 获取变更时间
     *
     * @return add_time - 变更时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置变更时间
     *
     * @param add_time 变更时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }
}