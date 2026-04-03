package com.laiketui.domain.divideAccount;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_mch_distribution")
public class MchDistributionModel implements Serializable
{

    /**
     * 分账接收方类型
     */
    public interface Type
    {
        /**
         * 商户号
         */
        String MERCHANT_ID         = "MERCHANT_ID";
        /**
         * 个人openid
         */
        String PERSONAL_OPENID     = "PERSONAL_OPENID";
        /**
         * 个人sub_openid
         */
        String PERSONAL_SUB_OPENID = "PERSONAL_SUB_OPENID";
    }

    /**
     * 分账接收方关系
     */
    public interface Relationship
    {
        /**
         * 服务商
         */
        String SERVICE_PROVIDER = "SERVICE_PROVIDER";
        /**
         * 门店
         */
        String STORE            = "STORE";
        /**
         * 员工
         */
        String STAFF            = "STAFF";
        /**
         * 店主
         */
        String STORE_OWNER      = "STORE_OWNER";
        /**
         * 合作伙伴
         */
        String PARTNER          = "PARTNER";
        /**
         * 总部
         */
        String HEADQUARTER      = "HEADQUARTER";
        /**
         * 品牌方
         */
        String BRAND            = "BRAND";
        /**
         * 分销商
         */
        String DISTRIBUTOR      = "DISTRIBUTOR";
        /**
         * 用户
         */
        String USER             = "USER";
        /**
         * 供应商
         */
        String SUPPLIER         = "SUPPLIER";
        /**
         * 自定义
         */
        String CUSTOM           = "CUSTOM";

    }


    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
     */
    private String d_type;

    /**
     * 分账接收方账号
     */
    private String account;

    /**
     * 分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义
     */
    private String relationship;

    /**
     * 分账比例
     */
    private BigDecimal proportion;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 分账接收方全称
     */
    private String name;


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
     * 获取店铺id
     *
     * @return mch_id - 店铺id
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店铺id
     *
     * @param mch_id 店铺id
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
     *
     * @return d_type - 分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
     */
    public String getD_type()
    {
        return d_type;
    }

    /**
     * 设置分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
     *
     * @param d_type 分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）
     */
    public void setD_type(String d_type)
    {
        this.d_type = d_type == null ? null : d_type.trim();
    }

    /**
     * 获取分账接收方账号
     *
     * @return account - 分账接收方账号
     */
    public String getAccount()
    {
        return account;
    }

    /**
     * 设置分账接收方账号
     *
     * @param account 分账接收方账号
     */
    public void setAccount(String account)
    {
        this.account = account == null ? null : account.trim();
    }

    /**
     * 获取分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义
     *
     * @return relationship - 分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义
     */
    public String getRelationship()
    {
        return relationship;
    }

    /**
     * 设置分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义
     *
     * @param relationship 分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义
     */
    public void setRelationship(String relationship)
    {
        this.relationship = relationship == null ? null : relationship.trim();
    }

    /**
     * 获取分账比例
     *
     * @return proportion - 分账比例
     */
    public BigDecimal getProportion()
    {
        return proportion;
    }

    /**
     * 设置分账比例
     *
     * @param proportion 分账比例
     */
    public void setProportion(BigDecimal proportion)
    {
        this.proportion = proportion;
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
}