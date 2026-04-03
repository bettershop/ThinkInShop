package com.laiketui.domain.supplier;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_supplier_account_log")
public class SupplierAccountLogModel implements Serializable
{

    /**
     * 入账
     */
    @Transient
    public static final int ENTRY = 1;
    /**
     * 出账
     */
    @Transient
    public static final int OUT   = 2;

    /**
     * 订单
     */
    @Transient
    public static final int ORDER = 1;

    /**
     * 退款
     */
    @Transient
    public static final int REFUND = 2;

    /**
     * 提现
     */
    @Transient
    public static final int WITHDRAWAL = 3;

    /**
     * 提现失败
     */
    @Transient
    public static final int WITHDRAWALFAIL = 4;

    /**
     * 订单运费
     */
    @Transient
    public static final int ORDERFRIGHT = 5;

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
     * 供应商id
     */
    private Integer supplier_id;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 供应商余额
     */
    private BigDecimal account_money;

    /**
     * 状态：1.入账 2.出账
     */
    private Integer status;

    /**
     * 类型：1.订单 2.退款 3.提现
     */
    private Integer type;

    /**
     * 时间
     */
    private Date add_time;

    /**
     * 订单号
     */
    private String remake;

    /**
     * 备注
     */
    private String remark;

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
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
     * 获取供应商id
     *
     * @return supplier_id - 供应商id
     */
    public Integer getSupplier_id()
    {
        return supplier_id;
    }

    /**
     * 设置供应商id
     *
     * @param supplier_id 供应商id
     */
    public void setSupplier_id(Integer supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    /**
     * 获取金额
     *
     * @return amount - 金额
     */
    public BigDecimal getAmount()
    {
        return amount;
    }

    /**
     * 设置金额
     *
     * @param amount 金额
     */
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    /**
     * 获取供应商余额
     *
     * @return account_money - 供应商余额
     */
    public BigDecimal getAccount_money()
    {
        return account_money;
    }

    /**
     * 设置供应商余额
     *
     * @param account_money 供应商余额
     */
    public void setAccount_money(BigDecimal account_money)
    {
        this.account_money = account_money;
    }

    /**
     * 获取状态：1.入账 2.出账
     *
     * @return status - 状态：1.入账 2.出账
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置状态：1.入账 2.出账
     *
     * @param status 状态：1.入账 2.出账
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取类型：1.订单 2.退款 3.提现
     *
     * @return type - 类型：1.订单 2.退款 3.提现
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置类型：1.订单 2.退款 3.提现
     *
     * @param type 类型：1.订单 2.退款 3.提现
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取时间
     *
     * @return addtime - 时间
     */
    public Date getAddtime()
    {
        return add_time;
    }

    /**
     * 设置时间
     *
     * @param addtime 时间
     */
    public void setAddtime(Date addtime)
    {
        this.add_time = addtime;
    }

    /**
     * 获取订单号
     *
     * @return remake - 订单号
     */
    public String getRemake()
    {
        return remake;
    }

    /**
     * 设置订单号
     *
     * @param remake 订单号
     */
    public void setRemake(String remake)
    {
        this.remake = remake == null ? null : remake.trim();
    }
}