package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 登录参数
 *
 * @author Trick
 * @date 2021/5/26 9:42
 */
@ApiModel(description = "登录参数")
public class LoginVo extends MainVo
{
    @ApiModelProperty(value = "账号/手机号", name = "login")
    private String  login;
    @ApiModelProperty(value = "密码", name = "pwd")
    private String  pwd;
    @ApiModelProperty(value = "商城查看经营收益", name = "storeLook")
    private Integer storeLook;
    @ApiModelProperty(value = "图形验证码", name = "imgCode")
    private String  imgCode;
    @ApiModelProperty(value = "图形验证码token", name = "imgCodeToken")
    private String  imgCodeToken;

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }

    public Integer getStoreLook()
    {
        return storeLook;
    }

    public void setStoreLook(Integer storeLook)
    {
        this.storeLook = storeLook;
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
