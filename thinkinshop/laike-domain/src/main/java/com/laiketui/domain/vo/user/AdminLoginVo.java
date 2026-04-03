package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 后台登录
 *
 * @author Trick
 * @date 2021/6/15 15:55
 */
@ApiModel(description = "后台登录")
public class AdminLoginVo extends MainVo
{
    @ApiModelProperty(value = "客户编号", name = "customerNumber")
    private String  customerNumber;
    @ApiModelProperty(value = "账号/用户名", name = "userName")
    private String  userName;
    @ApiModelProperty(value = "密码", name = "pwd")
    private String  pwd;
    @ApiModelProperty(value = "是否自动登录", name = "isAuto")
    private Integer isAuto;
    @ApiModelProperty(value = "图形验证码", name = "imgCode")
    private String  imgCode;
    @ApiModelProperty(value = "图形验证码token", name = "imgCodeToken")
    private String  imgCodeToken;

    public String getCustomerNumber()
    {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber)
    {
        this.customerNumber = customerNumber;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }

    public Integer getIsAuto()
    {
        return isAuto;
    }

    public void setIsAuto(Integer isAuto)
    {
        this.isAuto = isAuto;
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
}
