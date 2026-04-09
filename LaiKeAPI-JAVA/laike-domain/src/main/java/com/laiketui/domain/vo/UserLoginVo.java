package com.laiketui.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 用户登陆参数
 *
 * @author Trick
 * @date 2020/11/6 15:42
 */
@ApiModel(description = "用户登陆参数")
public class UserLoginVo extends MainVo
{

    @ApiModelProperty(value = "推送客户端ID", name = "clientid")
    private String  clientid;
    @ApiModelProperty(value = "电话号码", name = "phone")
    private String  phone;
    @ApiModelProperty(value = "密码", name = "password")
    private String  password;
    @ApiModelProperty(value = "推荐人userid", name = "pid")
    private String  pid;
    @ApiModelProperty(value = "是否自动登录 1=自动登录", name = "isAuto")
    private Integer isAuto = 0;
    @ApiModelProperty(value = "图形验证码", name = "imgCode")
    private String  imgCode;
    @ApiModelProperty(value = "图形验证码token", name = "imgCodeToken")
    private String  verificationCode;

    @ApiModelProperty(value = "区号")
    private String cpc;

    @ApiModelProperty(value = "登录类型 1：手机号 2：邮箱 3：账号")
    private Integer type;

    @ApiModelProperty(value = "邮箱")
    private String e_mail;

    public String getE_mail() {
        return e_mail;
    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getIsAuto()
    {
        return isAuto;
    }

    public void setIsAuto(Integer isAuto)
    {
        this.isAuto = isAuto;
    }

    public String getClientid()
    {
        return clientid;
    }

    public void setClientid(String clientid)
    {
        this.clientid = clientid;
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

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public String getImgCode()
    {
        return imgCode;
    }

    public void setImgCode(String imgCode)
    {
        this.imgCode = imgCode;
    }

    public String getVerificationCode()
    {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode)
    {
        this.verificationCode = verificationCode;
    }
}
