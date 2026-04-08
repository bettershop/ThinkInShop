package com.laiketui.domain.vo.invoice;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/7/28
 */
@ApiModel(description = "添加/修改发票抬头信息")
public class InvoiceHeaderVo extends MainVo
{

    @ApiModelProperty(value = "id(修改传)", name = "id")
    private Integer id;
    @ApiModelProperty(value = "抬头类型 1.企业 2.个人", name = "type")
    private Integer type;
    @ApiModelProperty(value = "公司名称(抬头名称)", name = "companyName")
    private String  companyName;
    @ApiModelProperty(value = "公司税号", name = "companyTaxNumber")
    private String  companyTaxNumber;
    @ApiModelProperty(value = "注册地址", name = "registerAddress")
    private String  registerAddress;
    @ApiModelProperty(value = "注册电话", name = "registerPhone")
    private String  registerPhone;
    @ApiModelProperty(value = "开户银行", name = "depositBank")
    private String  depositBank;
    @ApiModelProperty(value = "银行卡账号", name = "bankNumber")
    private String  bankNumber;
    @ApiModelProperty(value = "是否默认 0.否 1.是", name = "isDefault")
    private Integer isDefault;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
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

    public String getRegisterAddress()
    {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress)
    {
        this.registerAddress = registerAddress;
    }

    public String getRegisterPhone()
    {
        return registerPhone;
    }

    public void setRegisterPhone(String registerPhone)
    {
        this.registerPhone = registerPhone;
    }

    public String getDepositBank()
    {
        return depositBank;
    }

    public void setDepositBank(String depositBank)
    {
        this.depositBank = depositBank;
    }

    public String getBankNumber()
    {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber)
    {
        this.bankNumber = bankNumber;
    }

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }
}
