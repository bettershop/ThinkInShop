package com.laiketui.domain.invoice;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_invoice_header")
public class InvoiceHeaderModel implements Serializable
{

    /**
     * 企业
     */
    @Transient
    public final static Integer ENTERPRISE = 1;

    /**
     * 个人
     */
    @Transient
    public final static Integer PERSONAL = 2;

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
     * 用户id
     */
    private String user_id;

    /**
     * 抬头类型 1.企业 2.个人
     */
    private Integer type;

    /**
     * 公司名称(抬头名称)
     */
    private String company_name;

    /**
     * 公司税号
     */
    private String company_tax_number;

    /**
     * 注册地址
     */
    private String register_address;

    /**
     * 注册电话
     */
    private String register_phone;

    /**
     * 开户银行
     */
    private String deposit_bank;

    /**
     * 银行卡账号
     */
    private String bank_number;

    /**
     * 是否默认 0.否 1.是
     */
    private Integer is_default;

    /**
     * 是否回收 0.否 1.是
     */
    private Integer recovery;

    /**
     * 添加时间
     */
    private Date add_time;

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
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户id
     *
     * @param user_id 用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取抬头类型 1.企业 2.个人
     *
     * @return type - 抬头类型 1.企业 2.个人
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置抬头类型 1.企业 2.个人
     *
     * @param type 抬头类型 1.企业 2.个人
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取公司名称(抬头名称)
     *
     * @return company_name - 公司名称(抬头名称)
     */
    public String getCompany_name()
    {
        return company_name;
    }

    /**
     * 设置公司名称(抬头名称)
     *
     * @param company_name 公司名称(抬头名称)
     */
    public void setCompany_name(String company_name)
    {
        this.company_name = company_name == null ? null : company_name.trim();
    }

    /**
     * 获取公司税号
     *
     * @return company_tax_number - 公司税号
     */
    public String getCompany_tax_number()
    {
        return company_tax_number;
    }

    /**
     * 设置公司税号
     *
     * @param company_tax_number 公司税号
     */
    public void setCompany_tax_number(String company_tax_number)
    {
        this.company_tax_number = company_tax_number == null ? null : company_tax_number.trim();
    }

    /**
     * 获取注册地址
     *
     * @return register_address - 注册地址
     */
    public String getRegister_address()
    {
        return register_address;
    }

    /**
     * 设置注册地址
     *
     * @param register_address 注册地址
     */
    public void setRegister_address(String register_address)
    {
        this.register_address = register_address == null ? null : register_address.trim();
    }

    /**
     * 获取注册电话
     *
     * @return register_phone - 注册电话
     */
    public String getRegister_phone()
    {
        return register_phone;
    }

    /**
     * 设置注册电话
     *
     * @param register_phone 注册电话
     */
    public void setRegister_phone(String register_phone)
    {
        this.register_phone = register_phone == null ? null : register_phone.trim();
    }

    /**
     * 获取开户银行
     *
     * @return deposit_bank - 开户银行
     */
    public String getDeposit_bank()
    {
        return deposit_bank;
    }

    /**
     * 设置开户银行
     *
     * @param deposit_bank 开户银行
     */
    public void setDeposit_bank(String deposit_bank)
    {
        this.deposit_bank = deposit_bank == null ? null : deposit_bank.trim();
    }

    /**
     * 获取银行卡账号
     *
     * @return bank_number - 银行卡账号
     */
    public String getBank_number()
    {
        return bank_number;
    }

    /**
     * 设置银行卡账号
     *
     * @param bank_number 银行卡账号
     */
    public void setBank_number(String bank_number)
    {
        this.bank_number = bank_number == null ? null : bank_number.trim();
    }

    /**
     * 获取是否默认 0.否 1.是
     *
     * @return is_default - 是否默认 0.否 1.是
     */
    public Integer getIs_default()
    {
        return is_default;
    }

    /**
     * 设置是否默认 0.否 1.是
     *
     * @param is_default 是否默认 0.否 1.是
     */
    public void setIs_default(Integer is_default)
    {
        this.is_default = is_default;
    }

    /**
     * 获取是否回收 0.否 1.是
     *
     * @return recovery - 是否回收 0.否 1.是
     */
    public Integer getRecovery()
    {
        return recovery;
    }

    /**
     * 设置是否回收 0.否 1.是
     *
     * @param recovery 是否回收 0.否 1.是
     */
    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
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
}