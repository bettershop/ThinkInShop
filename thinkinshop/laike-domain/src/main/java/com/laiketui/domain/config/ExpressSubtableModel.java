package com.laiketui.domain.config;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_express_subtable")
public class ExpressSubtableModel implements Serializable
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
     * 店铺ID
     */
    private Integer mchId;

    /**
     * 主表ID
     */
    private Integer expressId;

    /**
     * 电子面单客户账户或月结账号
     */
    private String partnerId;

    /**
     * 电子面单密码
     */
    private String partnerKey;

    /**
     * 电子面单密钥
     */
    private String partnerSecret;

    /**
     * 电子面单客户账户名称
     */
    private String partnerName;

    /**
     * 收件网点名称
     */
    private String expressNet;

    /**
     * 电子面单承载编号
     */
    private String expressCode;

    /**
     * 电子面单承载快递员名
     */
    private String checkMan;

    /**
     * 添加时间
     */
    private Date addTime;

    /**
     * 模板id
     */
    private String temp_id;

    /**
     * 是否删除 0.未删除 1.已删除
     */
    private Integer recovery;


    public String getTemp_id() {
        return temp_id;
    }

    public void setTemp_id(String temp_id) {
        this.temp_id = temp_id;
    }

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

    public Integer getExpressId()
    {
        return expressId;
    }

    public void setExpressId(Integer expressId)
    {
        this.expressId = expressId;
    }

    public String getPartnerId()
    {
        return partnerId;
    }

    public void setPartnerId(String partnerId)
    {
        this.partnerId = partnerId;
    }

    public String getPartnerKey()
    {
        return partnerKey;
    }

    public void setPartnerKey(String partnerKey)
    {
        this.partnerKey = partnerKey;
    }

    public String getPartnerSecret()
    {
        return partnerSecret;
    }

    public void setPartnerSecret(String partnerSecret)
    {
        this.partnerSecret = partnerSecret;
    }

    public String getPartnerName()
    {
        return partnerName;
    }

    public void setPartnerName(String partnerName)
    {
        this.partnerName = partnerName;
    }

    public String getExpressNet()
    {
        return expressNet;
    }

    public void setExpressNet(String expressNet)
    {
        this.expressNet = expressNet;
    }

    public String getExpressCode()
    {
        return expressCode;
    }

    public void setExpressCode(String expressCode)
    {
        this.expressCode = expressCode;
    }

    public String getCheckMan()
    {
        return checkMan;
    }

    public void setCheckMan(String checkMan)
    {
        this.checkMan = checkMan;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }

    public Integer getRecovery()
    {
        return recovery;
    }

    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }
}
