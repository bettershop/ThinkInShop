package com.laiketui.domain.vo.main;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 添加/修改 商城配置
 *
 * @author Trick
 * @date 2021/1/27 17:48
 */
@ApiModel(description = "添加/修改 商城配置")
public class AddStoreConfigVo extends MainVo
{
    @ApiModelProperty(value = "是否开启插件", name = "isOpenPlugin")
    private Integer    isOpenPlugin;
    @ApiModelProperty(value = "店铺默认logo", name = "logiUrl")
    private String     logiUrl;
    @ApiModelProperty(value = "店铺自动审核天数", name = "autoExamine")
    private Integer    autoExamine;
    @ApiModelProperty(value = "删除天数设置", name = "outDayDel")
    private Integer    outDayDel;
    @ApiModelProperty(value = "上传方式 1.上传商品 2.自选商品", name = "uploadType")
    private String     uploadType;
    @ApiModelProperty(value = "最低提现金额", name = "minWithdrawalMoney")
    private BigDecimal minWithdrawalMoney;
    @ApiModelProperty(value = "最高提现金额", name = "maxWithdrawalMoney")
    private BigDecimal maxWithdrawalMoney;
    @ApiModelProperty(value = "手续费为大于0小于1的小数,如0.05", name = "serviceCharge")
    private BigDecimal serviceCharge;
    @ApiModelProperty(value = "提现说明", name = "illustrate")
    private String     illustrate;
    @ApiModelProperty(value = "保证金开关 1=true 0=false", name = "promiseSwitch")
    private Integer    promiseSwitch = 0;
    @ApiModelProperty(value = "保证金金额", name = "promiseAmt")
    private BigDecimal promiseAmt;
    @ApiModelProperty(value = "保证金说明", name = "promiseText")
    private String     promiseText;
    @ApiModelProperty(value = "宣传图", name = "posterImg")
    private String     posterImg;

    @ApiModelProperty(value = "头像", name = "headImg")
    private String  headImg;
    @ApiModelProperty(value = "自动注销时间(月)", name = "autoLogOff")
    private Integer autoLogOff;
    @ApiModelProperty(value = "提现时间开关 0.不限制 1.指定日期 2.指定时间段", name = "withdrawalTimeOpen")
    private Integer withdrawalTimeOpen;
    @ApiModelProperty(value = "指定时间(时间段:15-20)", name = "withdrawalTime")
    private String  withdrawalTime;

    public Integer getAutoLogOff()
    {
        return autoLogOff;
    }

    public void setAutoLogOff(Integer autoLogOff)
    {
        this.autoLogOff = autoLogOff;
    }

    public Integer getWithdrawalTimeOpen()
    {
        return withdrawalTimeOpen;
    }

    public void setWithdrawalTimeOpen(Integer withdrawalTimeOpen)
    {
        this.withdrawalTimeOpen = withdrawalTimeOpen;
    }

    public String getWithdrawalTime()
    {
        return withdrawalTime;
    }

    public void setWithdrawalTime(String withdrawalTime)
    {
        this.withdrawalTime = withdrawalTime;
    }

    public Integer getAutoExamine()
    {
        return autoExamine;
    }

    public void setAutoExamine(Integer autoExamine)
    {
        this.autoExamine = autoExamine;
    }

    public String getPosterImg()
    {
        return posterImg;
    }

    public void setPosterImg(String posterImg)
    {
        this.posterImg = posterImg;
    }

    public String getHeadImg()
    {
        return headImg;
    }

    public void setHeadImg(String headImg)
    {
        this.headImg = headImg;
    }

    public String getPromiseText()
    {
        return promiseText;
    }

    public void setPromiseText(String promiseText)
    {
        this.promiseText = promiseText;
    }

    public Integer getPromiseSwitch()
    {
        return promiseSwitch;
    }

    public void setPromiseSwitch(Integer promiseSwitch)
    {
        this.promiseSwitch = promiseSwitch;
    }

    public BigDecimal getPromiseAmt()
    {
        return promiseAmt;
    }

    public void setPromiseAmt(BigDecimal promiseAmt)
    {
        this.promiseAmt = promiseAmt;
    }

    public String getIllustrate()
    {
        return illustrate;
    }

    public void setIllustrate(String illustrate)
    {
        this.illustrate = illustrate;
    }

    public Integer getIsOpenPlugin()
    {
        return isOpenPlugin;
    }

    public void setIsOpenPlugin(Integer isOpenPlugin)
    {
        this.isOpenPlugin = isOpenPlugin;
    }

    public String getLogiUrl()
    {
        return logiUrl;
    }

    public void setLogiUrl(String logiUrl)
    {
        this.logiUrl = logiUrl;
    }

    public Integer getOutDayDel()
    {
        return outDayDel;
    }

    public void setOutDayDel(Integer outDayDel)
    {
        this.outDayDel = outDayDel;
    }

    public String getUploadType()
    {
        return uploadType;
    }

    public void setUploadType(String uploadType)
    {
        this.uploadType = uploadType;
    }

    public BigDecimal getMinWithdrawalMoney()
    {
        return minWithdrawalMoney;
    }

    public void setMinWithdrawalMoney(BigDecimal minWithdrawalMoney)
    {
        this.minWithdrawalMoney = minWithdrawalMoney;
    }

    public BigDecimal getMaxWithdrawalMoney()
    {
        return maxWithdrawalMoney;
    }

    public void setMaxWithdrawalMoney(BigDecimal maxWithdrawalMoney)
    {
        this.maxWithdrawalMoney = maxWithdrawalMoney;
    }

    public BigDecimal getServiceCharge()
    {
        return serviceCharge;
    }

    public void setServiceCharge(BigDecimal serviceCharge)
    {
        this.serviceCharge = serviceCharge;
    }
}
