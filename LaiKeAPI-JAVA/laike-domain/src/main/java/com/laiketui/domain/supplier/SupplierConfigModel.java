package com.laiketui.domain.supplier;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "lkt_supplier_config")
public class SupplierConfigModel implements Serializable
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
     * 最小充值金额
     */
    private BigDecimal min_recharge;

    /**
     * 最小提现金额
     */
    private BigDecimal min_withdrawal;

    /**
     * 最大提现金额
     */
    private BigDecimal max_withdrawal;

    /**
     * 手续费
     */
    private BigDecimal commission;

    /**
     * 钱包单位
     */
    private String wallet_unit;

    /**
     * 提现说明
     */
    private String withdrawal_ins;

    /**
     * 供应商商品是否需要审核 0.否 1.是
     */
    private Integer is_examine;

    public String getWithdrawal_ins()
    {
        return withdrawal_ins;
    }

    public void setWithdrawal_ins(String withdrawal_ins)
    {
        this.withdrawal_ins = withdrawal_ins;
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
     * 获取最小充值金额
     *
     * @return min_recharge - 最小充值金额
     */
    public BigDecimal getMin_recharge()
    {
        return min_recharge;
    }

    /**
     * 设置最小充值金额
     *
     * @param min_recharge 最小充值金额
     */
    public void setMin_recharge(BigDecimal min_recharge)
    {
        this.min_recharge = min_recharge;
    }

    /**
     * 获取最小提现金额
     *
     * @return min_withdrawal - 最小提现金额
     */
    public BigDecimal getMin_withdrawal()
    {
        return min_withdrawal;
    }

    /**
     * 设置最小提现金额
     *
     * @param min_withdrawal 最小提现金额
     */
    public void setMin_withdrawal(BigDecimal min_withdrawal)
    {
        this.min_withdrawal = min_withdrawal;
    }

    /**
     * 获取最大提现金额
     *
     * @return max_withdrawal - 最大提现金额
     */
    public BigDecimal getMax_withdrawal()
    {
        return max_withdrawal;
    }

    /**
     * 设置最大提现金额
     *
     * @param max_withdrawal 最大提现金额
     */
    public void setMax_withdrawal(BigDecimal max_withdrawal)
    {
        this.max_withdrawal = max_withdrawal;
    }

    /**
     * 获取手续费
     *
     * @return commission - 手续费
     */
    public BigDecimal getCommission()
    {
        return commission;
    }

    /**
     * 设置手续费
     *
     * @param commission 手续费
     */
    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    /**
     * 获取钱包单位
     *
     * @return wallet_unit - 钱包单位
     */
    public String getWallet_unit()
    {
        return wallet_unit;
    }

    /**
     * 设置钱包单位
     *
     * @param wallet_unit 钱包单位
     */
    public void setWallet_unit(String wallet_unit)
    {
        this.wallet_unit = wallet_unit == null ? null : wallet_unit.trim();
    }

    public Integer getIs_examine()
    {
        return is_examine;
    }

    public void setIs_examine(Integer is_examine)
    {
        this.is_examine = is_examine;
    }
}