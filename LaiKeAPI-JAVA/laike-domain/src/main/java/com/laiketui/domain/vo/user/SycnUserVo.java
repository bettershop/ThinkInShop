package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加银行卡
 *
 * @author vvx
 * @date 2024/05/24 15:39
 */
@ApiModel(description = "用户同步登陆")
public class SycnUserVo extends MainVo
{

    @ApiModelProperty(value = "第三方用户ID", name = "user_from_id")
    private String user_from_id;

    @ApiModelProperty(value = "第三方用户名称", name = "user_name")
    private String user_name;

    @ApiModelProperty(value = "来源", name = "source")

    /**
     * 来源 1.小程序 11.app 6.pc商城端 2.H5
     */
    private int source;

    @ApiModelProperty(value = "手机号", name = "phone")
    private String phone;

    @ApiModelProperty(value = "手机号", name = "mobile")
    private String mobile;

    public String getUser_from_id()
    {
        return user_from_id;
    }

    public void setUser_from_id(String user_from_id)
    {
        this.user_from_id = user_from_id;
    }

    public int getSource()
    {
        return source;
    }

    public void setSource(int source)
    {
        this.source = source;
    }

    public String getUser_name()
    {
        return user_name;
    }

    public void setUser_name(String user_name)
    {
        this.user_name = user_name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
