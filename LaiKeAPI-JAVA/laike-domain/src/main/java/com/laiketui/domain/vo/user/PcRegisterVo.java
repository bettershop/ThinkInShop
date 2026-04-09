package com.laiketui.domain.vo.user;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 商城注册参数
 *
 * @author Trick
 * @date 2021/6/16 16:05
 */
@ApiModel(description = "商城注册参数")
public class PcRegisterVo extends MainVo
{
    @ApiModelProperty(value = "推送客户端id", name = "clientId")
    private Integer clientId;
    @ApiModelProperty(value = "手机号", name = "phone")
    private String  phone;
    @ApiModelProperty(value = "验证码", name = "keyCode")
    private String  keyCode;
    @ApiModelProperty(value = "账号", name = "userName")
    @ParamsMapping("userId")
    private String  userName;
    @ApiModelProperty(value = "密码", name = "password")
    private String  password;
    @ApiModelProperty(value = "推荐人id", name = "pid")
    private String  pid;

    @ApiModelProperty(value = "注册类型 0：手机号 1:邮箱")
    private Integer type;

    @ApiModelProperty(value = "邮箱")
    private String e_mail;

    @ApiModelProperty(value = "图形验证码", name = "imgCode")
    private String imgCode;
    @ApiModelProperty(value = "图形验证码token", name = "imgCodeToken")
    private String imgCodeToken;

    public String getE_mail() {
        return e_mail;
    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImgCodeToken()
    {
        return imgCodeToken;
    }

    public void setImgCodeToken(String imgCodeToken)
    {
        this.imgCodeToken = imgCodeToken;
    }

    public String getImgCode()
    {
        return imgCode;
    }

    public void setImgCode(String imgCode)
    {
        this.imgCode = imgCode;
    }

    public Integer getClientId()
    {
        return clientId;
    }

    public void setClientId(Integer clientId)
    {
        this.clientId = clientId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getKeyCode()
    {
        return keyCode;
    }

    public void setKeyCode(String keyCode)
    {
        this.keyCode = keyCode;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
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
}
