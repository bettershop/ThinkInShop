package com.laiketui.domain.config;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 订单打印配置
 */
@Table(name = "lkt_print_setup")
public class PrintSetupModel implements Serializable
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
    private Integer storeId;

    /**
     * 店铺id
     */
    private Integer mchId;

    /**
     * 供应商id
     */
    private Integer supplierId;

    /**
     * 打印名称
     */
    private String printName;

    /**
     * 打印url
     */
    private String printUrl;

    /**
     * 省
     */
    private String sheng;

    /**
     * 市
     */
    private String shi;

    /**
     * 县
     */
    private String xian;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 打印电话
     */
    private String phone;

    /**
     * 添加时间
     */
    private Date addTime;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getPrintName()
    {
        return printName;
    }

    public void setPrintName(String printName)
    {
        this.printName = printName;
    }

    public String getPrintUrl()
    {
        return printUrl;
    }

    public void setPrintUrl(String printUrl)
    {
        this.printUrl = printUrl;
    }

    public String getSheng()
    {
        return sheng;
    }

    public void setSheng(String sheng)
    {
        this.sheng = sheng;
    }

    public String getShi()
    {
        return shi;
    }

    public void setShi(String shi)
    {
        this.shi = shi;
    }

    public String getXian()
    {
        return xian;
    }

    public void setXian(String xian)
    {
        this.xian = xian;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }

    public Integer getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId)
    {
        this.supplierId = supplierId;
    }
}
