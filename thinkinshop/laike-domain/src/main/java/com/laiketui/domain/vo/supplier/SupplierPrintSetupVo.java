package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 上传订单打印配置
 */
public class SupplierPrintSetupVo extends MainVo
{
    //订单打印配置
    @ApiModelProperty(value = "printName", name = "打印名称")
    private String printName;

    @ApiModelProperty(value = "printUrl", name = "打印url")
    private String printUrl;


    @ApiModelProperty(value = "sheng", name = "省")
    private String sheng;


    @ApiModelProperty(value = "shi", name = "shi")
    private String shi;


    @ApiModelProperty(value = "xian", name = "xian")
    private String xian;


    @ApiModelProperty(value = "address", name = "详细地址")
    private String address;


    @ApiModelProperty(value = "phone", name = "打印电话")
    private String phone;

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
}
