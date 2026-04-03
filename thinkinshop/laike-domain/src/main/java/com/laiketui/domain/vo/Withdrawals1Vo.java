package com.laiketui.domain.vo;

import com.laiketui.core.annotation.ParamsMapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

/**
 * 申请提现参数
 *
 * @author Trick
 * @date 2020/11/3 12:02
 */
@ApiModel(description = "申请提现参数")
public class Withdrawals1Vo extends MainVo
{

    @ApiModelProperty(value = "店铺id", name = "shop_id")
    @ParamsMapping("shop_id")
    private Integer shopId;
    @ApiModelProperty(value = "提现id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "银行卡id", name = "bank_id")
    @ParamsMapping({"bank_id", "bankCardId"})
    private Integer bankId;
    @ApiModelProperty(value = "提现金额", name = "amoney")
    private String  amoney;
    @ApiModelProperty(value = "手机号", name = "mobile")
    private String  mobile;
    @ApiModelProperty(value = "验证码", name = "keyCode")
    @ParamsMapping(value = "code")
    private String  keyCode;
    @ApiModelProperty(value = "提现类型 1银行卡  2微信余额 3贝宝余额", name = "withdrawStatus")
    private Integer withdrawStatus;
    @ApiModelProperty(value = "用户微信余额提现真实姓名", name = "userName")
    private String  userName;
    @ApiModelProperty(value = "插件提现", name = "pluginType")
    private String  pluginType;
    @ApiModelProperty(value = "贝宝邮箱", name = "email")
    private String  email;

    @ApiModelProperty(value = "Stripe账户ID", name = "stripeAccountId")
    @ParamsMapping({"stripe_account_id", "stripeAccountId", "account_id"})
    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    public void setWithdrawStatus(Integer withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public String getStripeAccountId()
    {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId)
    {
        this.stripeAccountId = stripeAccountId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Integer getShopId()
    {
        return shopId;
    }

    public void setShopId(Integer shopId)
    {
        this.shopId = shopId;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getBankId()
    {
        return bankId;
    }

    public void setBankId(Integer bankId)
    {
        this.bankId = bankId;
    }

    public String getAmoney()
    {
        return amoney;
    }

    public void setAmoney(String amoney)
    {
        this.amoney = amoney;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getKeyCode()
    {
        return keyCode;
    }

    public void setKeyCode(String keyCode)
    {
        this.keyCode = keyCode;
    }

    public Integer getWithdrawStatus()
    {
        return withdrawStatus;
    }

    public void setwithdrawStatus(Integer withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPluginType()
    {
        return pluginType;
    }

    public void setPluginType(String pluginType)
    {
        this.pluginType = pluginType;
    }

    @Override
    public String toString()
    {
        return "Withdrawals1Vo{" +
                "shopId=" + shopId +
                ", id=" + id +
                ", bankId=" + bankId +
                ", amoney='" + amoney + '\'' +
                ", mobile='" + mobile + '\'' +
                ", keyCode='" + keyCode + '\'' +
                ", withdrawStatus=" + withdrawStatus +
                ", userName='" + userName + '\'' +
                ", pluginType='" + pluginType + '\'' +
                '}';
    }
}
