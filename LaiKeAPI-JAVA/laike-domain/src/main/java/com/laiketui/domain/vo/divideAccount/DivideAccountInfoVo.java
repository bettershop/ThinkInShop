package com.laiketui.domain.vo.divideAccount;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 16:33 2023/8/8
 */
public class DivideAccountInfoVo
{

    @ApiModelProperty(name = "type", value = "分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）")
    @JSONField(alternateNames = {"d_type"})
    private String     type;
    @ApiModelProperty(name = "typeDesc", value = "分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）")
    private String     typeDesc;
    @ApiModelProperty(name = "account", value = "分账接收方账号")
    private String     account;
    @ApiModelProperty(name = "relationship", value = "分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义")
    private String     relationship;
    @ApiModelProperty(name = "relationshipDesc", value = "分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义")
    private String     relationshipDesc;
    @ApiModelProperty(name = "proportion", value = "分账比例")
    private BigDecimal proportion;

    @ApiModelProperty(name = "name", value = "分账接收方全称")
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTypeDesc()
    {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc)
    {
        this.typeDesc = typeDesc;
    }

    public String getRelationshipDesc()
    {
        return relationshipDesc;
    }

    public void setRelationshipDesc(String relationshipDesc)
    {
        this.relationshipDesc = relationshipDesc;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public String getRelationship()
    {
        return relationship;
    }

    public void setRelationship(String relationship)
    {
        this.relationship = relationship;
    }

    public BigDecimal getProportion()
    {
        return proportion;
    }

    public void setProportion(BigDecimal proportion)
    {
        this.proportion = proportion;
    }
}
