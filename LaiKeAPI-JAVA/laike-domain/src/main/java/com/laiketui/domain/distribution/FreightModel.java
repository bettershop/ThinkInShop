package com.laiketui.domain.distribution;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_freight")
public class FreightModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 不是默认
     */
    @Transient
    public static final Integer IS_DEFAULT_CLOSE = 0;
    /**
     * 是默认
     */
    @Transient
    public static final Integer IS_DEFAULT_OPEN  = 1;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型 0:件 1:重量
     */
    private Integer type;

    /**
     * 运费
     */
    private String freight;

    /**
     * 类型 0:不默认 1:默认
     */
    private Integer is_default;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 是否是包邮设置 0.未开启 1.开启
     */
    private Integer is_package_settings;


    /**
     * 包邮设置
     */
    private String package_settings;

    /**
     * 是否不配送 0.未开启 1.开启
     */
    private Integer is_no_delivery;

    /**
     * 不配送区域
     */
    private String no_delivery;

    /**
     * 供应商id
     */
    private Integer supplier_id;


    /**
     * 默认运费规则
     */
    private String default_freight;

    /**
     * 不匹配地区
     */
    @Column(name = "threeIdsList")
    private String threeIdsList;

    private String lang_code;

    private Integer country_num;

    public String getLang_code()
    {
        return lang_code;
    }

    public void setLang_code(String lang_code)
    {
        this.lang_code = lang_code;
    }

    public Integer getCountry_num()
    {
        return country_num;
    }

    public void setCountry_num(Integer country_num)
    {
        this.country_num = country_num;
    }

    public String getThreeIdsList()
    {
        return threeIdsList;
    }

    public void setThreeIdsList(String threeIdsList)
    {
        this.threeIdsList = threeIdsList;
    }

    public void setIs_package_settings(Integer is_package_settings)
    {
        this.is_package_settings = is_package_settings;
    }

    public void setPackage_settings(String package_settings)
    {
        this.package_settings = package_settings;
    }

    public void setIs_no_delivery(Integer is_no_delivery)
    {
        this.is_no_delivery = is_no_delivery;
    }

    public void setNo_delivery(String no_delivery)
    {
        this.no_delivery = no_delivery;
    }

    public Integer getIs_package_settings()
    {
        return is_package_settings;
    }

    public String getPackage_settings()
    {
        return package_settings;
    }

    public Integer getIs_no_delivery()
    {
        return is_no_delivery;
    }

    public String getNo_delivery()
    {
        return no_delivery;
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
     * 获取类型 0:件 1:重量
     *
     * @return type - 类型 0:件 1:重量
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置类型 0:件 1:重量
     *
     * @param type 类型 0:件 1:重量
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取运费
     *
     * @return freight - 运费
     */
    public String getFreight()
    {
        return freight;
    }

    /**
     * 设置运费
     *
     * @param freight 运费
     */
    public void setFreight(String freight)
    {
        this.freight = freight == null ? null : freight.trim();
    }

    /**
     * 获取类型 0:不默认 1:默认
     *
     * @return is_default - 类型 0:不默认 1:默认
     */
    public Integer getIs_default()
    {
        return is_default;
    }

    /**
     * 设置类型 0:不默认 1:默认
     *
     * @param is_default 类型 0:不默认 1:默认
     */
    public void setIs_default(Integer is_default)
    {
        this.is_default = is_default;
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

    public Integer getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public String getDefault_freight()
    {
        return default_freight;
    }

    public void setDefault_freight(String default_freight)
    {
        this.default_freight = default_freight;
    }
}