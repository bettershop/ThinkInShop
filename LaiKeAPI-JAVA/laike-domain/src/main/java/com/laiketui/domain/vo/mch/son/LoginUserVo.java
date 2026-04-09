package com.laiketui.domain.vo.mch.son;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 门店核销登录参数
 *
 * @author gp
 * @date 2024-02-01
 */
@ApiModel(description = "门店核销登录参数")
public class LoginUserVo extends MainVo
{
    @ApiModelProperty(value = "手机号", name = "phone")
    @ParamsMapping({"mobile"})
    private String  phone;
    @ApiModelProperty(value = "账号", name = "account_number")
    private String  account_number;
    @ApiModelProperty(value = "密码", name = "password")
    private String  password;
    @ApiModelProperty(value = "图形验证码", name = "imgCode")
    private String  imgCode;
    @ApiModelProperty(value = "图形验证码token", name = "imgCodeToken")
    private String  imgCodeToken;
    @ApiModelProperty(value = "店铺id", name = "mch_id")
    private String  mch_id;
    @ApiModelProperty(value = "门店id", name = "mch_store_id")
    private Integer mch_store_id;

    public String getAccount_number()
    {
        return account_number;
    }

    public void setAccount_number(String account_number)
    {
        this.account_number = account_number;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getImgCode()
    {
        return imgCode;
    }

    public void setImgCode(String imgCode)
    {
        this.imgCode = imgCode;
    }

    public String getImgCodeToken()
    {
        return imgCodeToken;
    }

    public void setImgCodeToken(String imgCodeToken)
    {
        this.imgCodeToken = imgCodeToken;
    }

    public String getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(String mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getMch_store_id()
    {
        return mch_store_id;
    }

    public void setMch_store_id(Integer mch_store_id)
    {
        this.mch_store_id = mch_store_id;
    }
}
