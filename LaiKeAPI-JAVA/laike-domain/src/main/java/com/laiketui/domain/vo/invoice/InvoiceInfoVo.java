package com.laiketui.domain.vo.invoice;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/7/28
 */
@ApiModel(description = "申请开票")
public class InvoiceInfoVo extends MainVo
{

    @ApiModelProperty(value = "发票id(重新申请传)", name = "id")
    private Integer    id;
    @ApiModelProperty(value = "发票抬头id", name = "id")
    private Integer    headId;
    @ApiModelProperty(value = "抬头类型 1.企业 2.个人", name = "type")
    private Integer    type;
    @ApiModelProperty(value = "公司名称(抬头名称)", name = "companyName")
    private String     companyName;
    @ApiModelProperty(value = "公司税号", name = "companyTaxNumber")
    private String     companyTaxNumber;
    @ApiModelProperty(value = "总金额", name = "amount")
    private BigDecimal amount;
    @ApiModelProperty(value = "订单号", name = "sNo")
    private String     sNo;
    @ApiModelProperty(value = "电子邮箱", name = "email")
    private String     email;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getHeadId()
    {
        return headId;
    }

    public void setHeadId(Integer headId)
    {
        this.headId = headId;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getCompanyTaxNumber()
    {
        return companyTaxNumber;
    }

    public void setCompanyTaxNumber(String companyTaxNumber)
    {
        this.companyTaxNumber = companyTaxNumber;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
