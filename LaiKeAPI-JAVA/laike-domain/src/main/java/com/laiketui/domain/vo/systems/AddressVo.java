package com.laiketui.domain.vo.systems;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/修改商城参数
 *
 * @author Trick
 * @date 2021/1/15 10:43
 */
@ApiModel(description = "添加/修改商城参数")
public class AddressVo extends MainVo
{
    @ApiModelProperty(value = "id", name = "地址id")
    private Integer id;

    @ApiModelProperty(value = "name", name = "名称")
    private String  name;
    @ApiModelProperty(value = "tel", name = "联系人")
    private String  tel;
    @ApiModelProperty(value = "type", name = "类型（1发货地址 2售后地址）")
    private Integer type = 1;
    @ApiModelProperty(value = "code", name = "邮政编码")
    private String  code;
    @ApiModelProperty(value = "address", name = "详细地址")
    private String  address;

    @ApiModelProperty(value = "shen", name = "省")
    private String shen;
    @ApiModelProperty(value = "shi", name = "市")
    private String shi;
    @ApiModelProperty(value = "xian", name = "县")
    private String xian;

    @ApiModelProperty(value = "isDefault", name = "是否默认 1=默认 0=非默认")
    private Integer isDefault;

    @ApiModelProperty(value = "国家代码")
    private Integer country_num;

    @ApiModelProperty(value = "省（国外）")
    private String province;

    @ApiModelProperty(value = "市（国外）")
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Integer getCountry_num() {
        return country_num;
    }

    public void setCountry_num(Integer country_num) {
        this.country_num = country_num;
    }
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getShen()
    {
        return shen;
    }

    public void setShen(String shen)
    {
        this.shen = shen;
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

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }
}
