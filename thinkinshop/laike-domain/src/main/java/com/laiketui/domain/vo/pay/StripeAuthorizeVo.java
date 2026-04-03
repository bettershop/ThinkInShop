package com.laiketui.domain.vo.pay;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;

/**
 * Stripe Connect 授权参数 VO
 * 用于获取 Stripe 授权链接时传递用户和商户信息
 */
public class StripeAuthorizeVo extends MainVo
{


    /**
     * 可选：预填充用户姓名（用于Stripe注册页自动填充）
     */
    @ParamsMapping({"userName"})
    private String userName;

    /**
     * 可选：预填充用户电话（用于Stripe注册页自动填充）
     */
    @ParamsMapping({"phone"})
    private String phone;

    /**
     * 可选：用户在平台的唯一标识（用于关联Stripe账户与平台用户，增强安全性）
     */
    @ParamsMapping({"platformUserId"})
    private String platformUserId;


    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPlatformUserId()
    {
        return platformUserId;
    }

    public void setPlatformUserId(String platformUserId)
    {
        this.platformUserId = platformUserId;
    }
}
