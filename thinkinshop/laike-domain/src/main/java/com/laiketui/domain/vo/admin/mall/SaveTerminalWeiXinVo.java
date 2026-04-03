package com.laiketui.domain.vo.admin.mall;


import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 保存App配置
 *
 * @author Trick
 * @date 2021/7/23 18:15
 */
@ApiModel(description = "保存App配置")
public class SaveTerminalWeiXinVo extends MainVo
{

    @ApiModelProperty(value = "小程序id", name = "appId")
    private String  appId;
    @ApiModelProperty(value = "小程序密钥", name = "appSecret")
    private String  appSecret;
    @ApiModelProperty(value = "购买成功通知", name = "paySuccess")
    private String  paySuccess;
    @ApiModelProperty(value = "发货提醒通知", name = "delivery")
    private String  delivery;
    @ApiModelProperty(value = "退款成功通知", name = "refundRes")
    @ParamsMapping("refund_res")
    private String  refundRes;
    @ApiModelProperty(value = "隐藏钱包", name = "hideWallet")
    private Integer hideWallet;
    @ApiModelProperty(value = "小程序首页标题", name = "appTitle")
    private String  appTitle;
    @ApiModelProperty(value = "小程序授权登录logo", name = "appLogo")
    private String  appLogo;

    public String getDelivery()
    {
        return delivery;
    }

    public void setDelivery(String delivery)
    {
        this.delivery = delivery;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getAppSecret()
    {
        return appSecret;
    }

    public void setAppSecret(String appSecret)
    {
        this.appSecret = appSecret;
    }

    public String getPaySuccess()
    {
        return paySuccess;
    }

    public void setPaySuccess(String paySuccess)
    {
        this.paySuccess = paySuccess;
    }

    public String getRefundRes()
    {
        return refundRes;
    }

    public void setRefundRes(String refundRes)
    {
        this.refundRes = refundRes;
    }

    public Integer getHideWallet()
    {
        return hideWallet;
    }

    public void setHideWallet(Integer hideWallet)
    {
        this.hideWallet = hideWallet;
    }

    public String getAppTitle()
    {
        return appTitle;
    }

    public void setAppTitle(String appTitle)
    {
        this.appTitle = appTitle;
    }

    public String getAppLogo()
    {
        return appLogo;
    }

    public void setAppLogo(String appLogo)
    {
        this.appLogo = appLogo;
    }
}
