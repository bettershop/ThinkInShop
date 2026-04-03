package com.laiketui.domain.supplier;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_supplier_bank_card")
public class SupplierBankCardModel implements Serializable
{
    /**
     * 主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 供应商id
     */
    private Integer supplier_id;

    /**
     * 开户人
     */
    @Column(name = "Cardholder")
    private String cardholder;

    /**
     * 银行名称
     */
    @Column(name = "Bank_name")
    private String bank_name;

    /**
     * 支行名称
     */
    private String branch;

    /**
     * 银行卡号
     */
    @Column(name = "Bank_card_number")
    private String bank_card_number;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 是否默认 1默认
     */
    private Integer is_default;

    /**
     * 回收站 0.不回收 1.回收
     */
    private Integer recycle;

    /**
     * 获取主键id
     *
     * @return id - 主键id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键id
     *
     * @param id 主键id
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
     * 获取开户人
     *
     * @return Cardholder - 开户人
     */
    public String getCardholder()
    {
        return cardholder;
    }

    /**
     * 设置开户人
     *
     * @param cardholder 开户人
     */
    public void setCardholder(String cardholder)
    {
        this.cardholder = cardholder == null ? null : cardholder.trim();
    }

    /**
     * 获取银行名称
     *
     * @return Bank_name - 银行名称
     */
    public String getBank_name()
    {
        return bank_name;
    }

    /**
     * 设置银行名称
     *
     * @param bank_name 银行名称
     */
    public void setBank_name(String bank_name)
    {
        this.bank_name = bank_name == null ? null : bank_name.trim();
    }

    /**
     * 获取支行名称
     *
     * @return branch - 支行名称
     */
    public String getBranch()
    {
        return branch;
    }

    /**
     * 设置支行名称
     *
     * @param branch 支行名称
     */
    public void setBranch(String branch)
    {
        this.branch = branch == null ? null : branch.trim();
    }

    /**
     * 获取银行卡号
     *
     * @return Bank_card_number - 银行卡号
     */
    public String getBank_card_number()
    {
        return bank_card_number;
    }

    /**
     * 设置银行卡号
     *
     * @param bank_card_number 银行卡号
     */
    public void setBank_card_number(String bank_card_number)
    {
        this.bank_card_number = bank_card_number == null ? null : bank_card_number.trim();
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
     * 获取是否默认 1默认
     *
     * @return is_default - 是否默认 1默认
     */
    public Integer getIs_default()
    {
        return is_default;
    }

    /**
     * 设置是否默认 1默认
     *
     * @param is_default 是否默认 1默认
     */
    public void setIs_default(Integer is_default)
    {
        this.is_default = is_default;
    }

    /**
     * 获取回收站 0.不回收 1.回收
     *
     * @return recycle - 回收站 0.不回收 1.回收
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收站 0.不回收 1.回收
     *
     * @param recycle 回收站 0.不回收 1.回收
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }
}