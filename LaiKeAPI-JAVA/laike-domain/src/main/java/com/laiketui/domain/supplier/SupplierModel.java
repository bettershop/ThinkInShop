package com.laiketui.domain.supplier;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_supplier")
public class SupplierModel implements Serializable
{

    /**
     * 正常
     */
    @Transient
    public static final int NORMAL  = 0;
    /**
     * 到期
     */
    @Transient
    public static final int EXPIRE  = 1;
    /**
     * 锁定
     */
    @Transient
    public static final int LOCKING = 2;

    /**
     * 个人
     */
    @Transient
    public static final int PERSONAL   = 1;
    /**
     * 企业
     */
    @Transient
    public static final int ENTERPRISE = 2;

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
     * 账号
     */
    private String account_number;

    /**
     * 密码
     */
    private String password;

    /**
     * LOGO
     */
    private String logo;

    /**
     * 头像
     */
    private String head_url;

    /**
     * 供应商名称
     */
    private String supplier_name;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 到期时间
     */
    private Date expire_date;

    /**
     * 供应商类型id
     */
    private Integer dic_id;

    /**
     * 经营范围
     */
    private String business_scope;

    /**
     * 所属性质1.个人 2.企业
     */
    private Integer type;

    /**
     * 营业执照
     */
    private String business_license;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 联系电话
     */
    private String contact_phone;

    /**
     * 省级
     */
    private String province;

    /**
     * 市级
     */
    private String city;

    /**
     * 区级
     */
    private String area;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态 0.正常 1.到期 2.锁定
     */
    private Integer status;

    /**
     * 是否回收 0.否 1.是
     */
    private Integer recovery;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 累计余额
     */
    private BigDecimal cumulative_balance;

    /**
     * 剩余余额
     */
    private BigDecimal surplus_balance;

    /**
     * 最后登录时间
     */
    private Date last_login_date;

    /**
     * 商城样式颜色
     */
    private String color;

    /**
     * 区号
     */
    private String cpc;

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
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

    public String getHead_url()
    {
        return head_url;
    }

    public void setHead_url(String head_url)
    {
        this.head_url = head_url;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取账号
     *
     * @return account_number - 账号
     */
    public String getAccount_number()
    {
        return account_number;
    }

    /**
     * 设置账号
     *
     * @param account_number 账号
     */
    public void setAccount_number(String account_number)
    {
        this.account_number = account_number == null ? null : account_number.trim();
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password)
    {
        this.password = password == null ? null : password.trim();
    }

    /**
     * 获取LOGO
     *
     * @return logo - LOGO
     */
    public String getLogo()
    {
        return logo;
    }

    /**
     * 设置LOGO
     *
     * @param logo LOGO
     */
    public void setLogo(String logo)
    {
        this.logo = logo == null ? null : logo.trim();
    }

    /**
     * 获取供应商名称
     *
     * @return supplier_name - 供应商名称
     */
    public String getSupplier_name()
    {
        return supplier_name;
    }

    /**
     * 设置供应商名称
     *
     * @param supplier_name 供应商名称
     */
    public void setSupplier_name(String supplier_name)
    {
        this.supplier_name = supplier_name == null ? null : supplier_name.trim();
    }

    /**
     * 获取价格
     *
     * @return price - 价格
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 设置价格
     *
     * @param price 价格
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /**
     * 获取到期时间
     *
     * @return expire_date - 到期时间
     */
    public Date getExpire_date()
    {
        return expire_date;
    }

    /**
     * 设置到期时间
     *
     * @param expire_date 到期时间
     */
    public void setExpire_date(Date expire_date)
    {
        this.expire_date = expire_date;
    }

    /**
     * 获取供应商类型id
     *
     * @return dic_id - 供应商类型id
     */
    public Integer getDic_id()
    {
        return dic_id;
    }

    /**
     * 设置供应商类型id
     *
     * @param dic_id 供应商类型id
     */
    public void setDic_id(Integer dic_id)
    {
        this.dic_id = dic_id;
    }

    /**
     * 获取经营范围
     *
     * @return business_scope - 经营范围
     */
    public String getBusiness_scope()
    {
        return business_scope;
    }

    /**
     * 设置经营范围
     *
     * @param business_scope 经营范围
     */
    public void setBusiness_scope(String business_scope)
    {
        this.business_scope = business_scope == null ? null : business_scope.trim();
    }

    /**
     * 获取所属性质1.个人 2.企业
     *
     * @return type - 所属性质1.个人 2.企业
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置所属性质1.个人 2.企业
     *
     * @param type 所属性质1.个人 2.企业
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取营业执照
     *
     * @return business_license - 营业执照
     */
    public String getBusiness_license()
    {
        return business_license;
    }

    /**
     * 设置营业执照
     *
     * @param business_license 营业执照
     */
    public void setBusiness_license(String business_license)
    {
        this.business_license = business_license == null ? null : business_license.trim();
    }

    /**
     * 获取联系人
     *
     * @return contacts - 联系人
     */
    public String getContacts()
    {
        return contacts;
    }

    /**
     * 设置联系人
     *
     * @param contacts 联系人
     */
    public void setContacts(String contacts)
    {
        this.contacts = contacts == null ? null : contacts.trim();
    }

    /**
     * 获取联系电话
     *
     * @return contact_phone - 联系电话
     */
    public String getContact_phone()
    {
        return contact_phone;
    }

    /**
     * 设置联系电话
     *
     * @param contact_phone 联系电话
     */
    public void setContact_phone(String contact_phone)
    {
        this.contact_phone = contact_phone == null ? null : contact_phone.trim();
    }

    /**
     * 获取省级
     *
     * @return province - 省级
     */
    public String getProvince()
    {
        return province;
    }

    /**
     * 设置省级
     *
     * @param province 省级
     */
    public void setProvince(String province)
    {
        this.province = province == null ? null : province.trim();
    }

    /**
     * 获取市级
     *
     * @return city - 市级
     */
    public String getCity()
    {
        return city;
    }

    /**
     * 设置市级
     *
     * @param city 市级
     */
    public void setCity(String city)
    {
        this.city = city == null ? null : city.trim();
    }

    /**
     * 获取区级
     *
     * @return area - 区级
     */
    public String getArea()
    {
        return area;
    }

    /**
     * 设置区级
     *
     * @param area 区级
     */
    public void setArea(String area)
    {
        this.area = area == null ? null : area.trim();
    }

    /**
     * 获取地址
     *
     * @return address - 地址
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * 设置地址
     *
     * @param address 地址
     */
    public void setAddress(String address)
    {
        this.address = address == null ? null : address.trim();
    }

    /**
     * 获取状态 0.正常 1.到期 2.锁定
     *
     * @return status - 状态 0.正常 1.到期 2.锁定
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置状态 0.正常 1.到期 2.锁定
     *
     * @param status 状态 0.正常 1.到期 2.锁定
     */
    public void setStatus(Integer status)
    {
        this.status = status;
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
     * 获取累计余额
     *
     * @return cumulative_balance - 累计余额
     */
    public BigDecimal getCumulative_balance()
    {
        return cumulative_balance;
    }

    /**
     * 设置累计余额
     *
     * @param cumulative_balance 累计余额
     */
    public void setCumulative_balance(BigDecimal cumulative_balance)
    {
        this.cumulative_balance = cumulative_balance;
    }

    /**
     * 获取剩余余额
     *
     * @return surplus_balance - 剩余余额
     */
    public BigDecimal getSurplus_balance()
    {
        return surplus_balance;
    }

    /**
     * 设置剩余余额
     *
     * @param surplus_balance 剩余余额
     */
    public void setSurplus_balance(BigDecimal surplus_balance)
    {
        this.surplus_balance = surplus_balance;
    }

    /**
     * 获取最后登录时间
     *
     * @return last_login_date - 最后登录时间
     */
    public Date getLast_login_date()
    {
        return last_login_date;
    }

    /**
     * 设置最后登录时间
     *
     * @param last_login_date 最后登录时间
     */
    public void setLast_login_date(Date last_login_date)
    {
        this.last_login_date = last_login_date;
    }
}
