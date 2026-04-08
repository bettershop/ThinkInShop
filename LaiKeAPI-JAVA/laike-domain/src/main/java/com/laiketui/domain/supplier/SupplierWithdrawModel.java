package com.laiketui.domain.supplier;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_supplier_withdraw")
public class SupplierWithdrawModel implements Serializable
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
     * 供应商id
     */
    private Integer supplier_id;

    /**
     * 名称
     */
    private String name;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 银行卡id
     */
    private Integer bank_id;

    /**
     * 提现金额
     */
    private BigDecimal money;

    /**
     * 剩余金额
     */
    private BigDecimal z_money;

    /**
     * 手续费
     */
    private BigDecimal s_charge;

    /**
     * 状态 0：审核中 1：审核通过 2：拒绝
     */
    private Integer status;

    /**
     * 申请时间
     */
    private Date add_date;

    /**
     * 拒绝原因
     */
    private String refuse;

    /**
     * 凭证
     */
    private String voucher;

    /**
     * 是否回收 0.未回收 1.回收
     */
    private Integer recovery;

    /**
     * 提现类型
     */
    public interface WITHDRAW_STATUS
    {
        //1银行卡
        Integer YHK    = 1;
        //微信余额
        Integer WX     = 2;
        //贝宝余额
        Integer PAYPAL = 3;
    }

    /**
     * 贝宝提现邮箱
     */
    private String email;

    /**
     * 提现类型 1银行卡  2微信余额  3贝宝余额
     */
    private Integer withdraw_status;

    public Integer getWithdrawStatus()
    {
        return withdraw_status;
    }

    public void setWithdrawStatus(Integer withdraw_status)
    {
        this.withdraw_status = withdraw_status;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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
     * 获取名称
     *
     * @return name - 名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取手机
     *
     * @return mobile - 手机
     */
    public String getMobile()
    {
        return mobile;
    }

    /**
     * 设置手机
     *
     * @param mobile 手机
     */
    public void setMobile(String mobile)
    {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * 获取银行卡id
     *
     * @return bank_id - 银行卡id
     */
    public Integer getBank_id()
    {
        return bank_id;
    }

    /**
     * 设置银行卡id
     *
     * @param bank_id 银行卡id
     */
    public void setBank_id(Integer bank_id)
    {
        this.bank_id = bank_id;
    }

    /**
     * 获取提现金额
     *
     * @return money - 提现金额
     */
    public BigDecimal getMoney()
    {
        return money;
    }

    /**
     * 设置提现金额
     *
     * @param money 提现金额
     */
    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    /**
     * 获取剩余金额
     *
     * @return z_money - 剩余金额
     */
    public BigDecimal getZ_money()
    {
        return z_money;
    }

    /**
     * 设置剩余金额
     *
     * @param z_money 剩余金额
     */
    public void setZ_money(BigDecimal z_money)
    {
        this.z_money = z_money;
    }

    /**
     * 获取手续费
     *
     * @return s_charge - 手续费
     */
    public BigDecimal getS_charge()
    {
        return s_charge;
    }

    /**
     * 设置手续费
     *
     * @param s_charge 手续费
     */
    public void setS_charge(BigDecimal s_charge)
    {
        this.s_charge = s_charge;
    }

    /**
     * 获取状态 0：审核中 1：审核通过 2：拒绝
     *
     * @return status - 状态 0：审核中 1：审核通过 2：拒绝
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置状态 0：审核中 1：审核通过 2：拒绝
     *
     * @param status 状态 0：审核中 1：审核通过 2：拒绝
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取申请时间
     *
     * @return add_date - 申请时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置申请时间
     *
     * @param add_date 申请时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    /**
     * 获取拒绝原因
     *
     * @return refuse - 拒绝原因
     */
    public String getRefuse()
    {
        return refuse;
    }

    /**
     * 设置拒绝原因
     *
     * @param refuse 拒绝原因
     */
    public void setRefuse(String refuse)
    {
        this.refuse = refuse == null ? null : refuse.trim();
    }

    public String getVoucher()
    {
        return voucher;
    }

    public void setVoucher(String voucher)
    {
        this.voucher = voucher;
    }

    /**
     * 获取是否回收 0.未回收 1.回收
     *
     * @return recovery - 是否回收 0.未回收 1.回收
     */
    public Integer getRecovery()
    {
        return recovery;
    }

    /**
     * 设置是否回收 0.未回收 1.回收
     *
     * @param recovery 是否回收 0.未回收 1.回收
     */
    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }
}
