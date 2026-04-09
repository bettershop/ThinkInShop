package com.laiketui.domain.plugin;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


/**
 * 分销用户信息修改日志
 *
 * @author Trick
 * @date 2021/2/5 9:54
 */
@Table(name = "lkt_distribution_log")
public class DistributionLogModel implements Serializable
{
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商户ID
     */
    private Integer store_id;

    /**
     * 用户ID
     */
    private String user_id;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 修改前值
     */
    private String old_value;

    /**
     * 修改后值
     */
    private String new_value;

    /**
     * 事件
     */
    private String event;

    /**
     * 添加时间
     */
    private Date add_time;

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
     * 获取商户ID
     *
     * @return store_id - 商户ID
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商户ID
     *
     * @param store_id 商户ID
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
     * 获取操作员
     *
     * @return operator - 操作员
     */
    public String getOperator()
    {
        return operator;
    }

    /**
     * 设置操作员
     *
     * @param operator 操作员
     */
    public void setOperator(String operator)
    {
        this.operator = operator == null ? null : operator.trim();
    }

    /**
     * 获取修改前值
     *
     * @return old_value - 修改前值
     */
    public String getOld_value()
    {
        return old_value;
    }

    /**
     * 设置修改前值
     *
     * @param old_value 修改前值
     */
    public void setOld_value(String old_value)
    {
        this.old_value = old_value == null ? null : old_value.trim();
    }

    /**
     * 获取修改后值
     *
     * @return new_value - 修改后值
     */
    public String getNew_value()
    {
        return new_value;
    }

    /**
     * 设置修改后值
     *
     * @param new_value 修改后值
     */
    public void setNew_value(String new_value)
    {
        this.new_value = new_value == null ? null : new_value.trim();
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

    public DistributionLogModel(Integer store_id, String user_id, String operator, String old_value, String new_value, String event)
    {
        this.store_id = store_id;
        this.user_id = user_id;
        this.operator = operator;
        this.old_value = old_value;
        this.new_value = new_value;
        this.event = event;
        this.add_time = new Date();
    }

    public DistributionLogModel()
    {
    }
}