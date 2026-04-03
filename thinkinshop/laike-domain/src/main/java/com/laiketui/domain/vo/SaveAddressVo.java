package com.laiketui.domain.vo;

import com.laiketui.core.annotation.ParamsMapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 保存地址参数
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@ApiModel(description = "保存地址参数")
public class SaveAddressVo extends MainVo
{
    @ApiModelProperty(value = "联系人", name = "user_name")
    @ParamsMapping("user_name")
    private String userName;
    @ApiModelProperty(value = "联系电话", name = "mobile")
    private String mobile;
    @ApiModelProperty(value = "详细地址", name = "address")
    private String address;
    @ApiModelProperty(value = "默认状态", name = "is_default")
    @ParamsMapping("is_default")
    private int    isDefault = 0;
    @ApiModelProperty(value = "省-市-县", name = "place")
    private String place;

    @ApiModelProperty(value = "区号", name = "cpc")
    private String cpc;

    @ApiModelProperty(value = "州")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("邮政编码")
    private String zip_code;

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public int getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(int isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }
}
