package com.laiketui.domain.distribution;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分销系统收益统计报表
 */
@Table(name = "lkt_distribution_income")
public class DistributionIncomeModel implements Serializable
{
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
     * 购买员
     */
    private String user_id;

    /**
     * 当日预估收益
     */
    private BigDecimal estimated_income;

    /**
     * 当日有效订单数量
     */
    private Integer order_num;

    /**
     * 当日订单金额
     */
    private BigDecimal order_price;

    /**
     * 当日新增客户  新增绑定永久关系
     */
    private Integer new_customer;

    /**
     * 当日新增邀请 新增绑定临时关系
     */
    private Integer new_invitation;

    /**
     * 添加时间
     */
    private Date add_time;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    public String getUser_id()
    {
        return user_id;
    }

    public void setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    public BigDecimal getEstimated_income()
    {
        return estimated_income;
    }

    public void setEstimated_income(BigDecimal estimated_income)
    {
        this.estimated_income = estimated_income;
    }

    public Integer getOrder_num()
    {
        return order_num;
    }

    public void setOrder_num(Integer order_num)
    {
        this.order_num = order_num;
    }

    public BigDecimal getOrder_price()
    {
        return order_price;
    }

    public void setOrder_price(BigDecimal order_price)
    {
        this.order_price = order_price;
    }

    public Integer getNew_customer()
    {
        return new_customer;
    }

    public void setNew_customer(Integer new_customer)
    {
        this.new_customer = new_customer;
    }

    public Integer getNew_invitation()
    {
        return new_invitation;
    }

    public void setNew_invitation(Integer new_invitation)
    {
        this.new_invitation = new_invitation;
    }

    public Date getAdd_time()
    {
        return add_time;
    }

    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }


}
