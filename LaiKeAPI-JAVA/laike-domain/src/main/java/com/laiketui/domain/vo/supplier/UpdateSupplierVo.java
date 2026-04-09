package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/9/14
 */
@ApiModel(description = "修改供应商信息")
public class UpdateSupplierVo extends MainVo
{

    @ApiModelProperty(value = "LOGO", name = "logo")
    private String  logo;
    @ApiModelProperty(value = "头像", name = "headUrl")
    private String  headUrl;
    @ApiModelProperty(value = "供应商名称", name = "supplierName")
    private String  supplierName;
    @ApiModelProperty(value = "经营范围", name = "businessScope")
    private String  businessScope;
    @ApiModelProperty(value = "所属性质1.个人 2.企业", name = "type")
    private Integer type;
    @ApiModelProperty(value = "营业执照", name = "businessLicense")
    private String  businessLicense;
    @ApiModelProperty(value = "联系人", name = "contacts")
    private String  contacts;
    @ApiModelProperty(value = "联系电话", name = "contactPhone")
    private String  contactPhone;
    @ApiModelProperty(value = "省级", name = "province")
    private String  province;
    @ApiModelProperty(value = "市级", name = "city")
    private String  city;
    @ApiModelProperty(value = "区级", name = "area")
    private String  area;
    @ApiModelProperty(value = "地址", name = "address")
    private String  address;

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getHeadUrl()
    {
        return headUrl;
    }

    public void setHeadUrl(String headUrl)
    {
        this.headUrl = headUrl;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getBusinessScope()
    {
        return businessScope;
    }

    public void setBusinessScope(String businessScope)
    {
        this.businessScope = businessScope;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getBusinessLicense()
    {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense)
    {
        this.businessLicense = businessLicense;
    }

    public String getContacts()
    {
        return contacts;
    }

    public void setContacts(String contacts)
    {
        this.contacts = contacts;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getArea()
    {
        return area;
    }

    public void setArea(String area)
    {
        this.area = area;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
}
