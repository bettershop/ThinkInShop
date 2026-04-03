package com.laiketui.domain.invoice;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_invoice_info")
public class InvoiceInfoModel implements Serializable
{


    /**
     * 申请中
     */
    @Transient
    public final static Integer IN_APPLICATION = 1;

    /**
     * 已完成
     */
    @Transient
    public final static Integer COMPLETED = 2;

    /**
     * 已撤销
     */
    @Transient
    public final static Integer RESCINDED = 3;


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
     * 店铺id
     */
    private Integer mch_id;

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
     * 发票抬头快照
     */
    private String invoice_header;

    /**
     * 订单号
     */
    private String sNo;

    /**
     * 发票金额
     */
    private BigDecimal invoice_amount;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 发票状态 1.申请中 2.已完成 3.已撤销
     */
    private Integer invoice_status;

    /**
     * 发票文件
     */
    private String file;

    /**
     * 上传发票文件时间
     */
    private Date file_time;

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

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
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
     * 获取发票抬头快照
     *
     * @return invoice_header - 发票抬头快照
     */
    public String getInvoice_header()
    {
        return invoice_header;
    }

    /**
     * 设置发票抬头快照
     *
     * @param invoice_header 发票抬头快照
     */
    public void setInvoice_header(String invoice_header)
    {
        this.invoice_header = invoice_header == null ? null : invoice_header.trim();
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
     * 获取发票金额
     *
     * @return invoice_amount - 发票金额
     */
    public BigDecimal getInvoice_amount()
    {
        return invoice_amount;
    }

    /**
     * 设置发票金额
     *
     * @param invoice_amount 发票金额
     */
    public void setInvoice_amount(BigDecimal invoice_amount)
    {
        this.invoice_amount = invoice_amount;
    }

    /**
     * 获取电子邮箱
     *
     * @return email - 电子邮箱
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * 设置电子邮箱
     *
     * @param email 电子邮箱
     */
    public void setEmail(String email)
    {
        this.email = email == null ? null : email.trim();
    }

    /**
     * 获取发票状态 1.申请中 2.已完成 3.已撤销
     *
     * @return invoice_status - 发票状态 1.申请中 2.已完成 3.已撤销
     */
    public Integer getInvoice_status()
    {
        return invoice_status;
    }

    /**
     * 设置发票状态 1.申请中 2.已完成 3.已撤销
     *
     * @param invoice_status 发票状态 1.申请中 2.已完成 3.已撤销
     */
    public void setInvoice_status(Integer invoice_status)
    {
        this.invoice_status = invoice_status;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public Date getFile_time()
    {
        return file_time;
    }

    public void setFile_time(Date file_time)
    {
        this.file_time = file_time;
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