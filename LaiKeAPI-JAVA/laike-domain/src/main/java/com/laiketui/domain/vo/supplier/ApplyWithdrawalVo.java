package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 申请提现参数
 *
 * @author sunH
 * @date 2022/10/09 12:02
 */
@ApiModel(description = "供应商申请提现参数")
public class ApplyWithdrawalVo extends MainVo
{
    @ApiModelProperty(value = "提现id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "银行卡id", name = "bank_id")
    private Integer    bankId;
    @ApiModelProperty(value = "提现金额", name = "amount")
    private BigDecimal amount;
    @ApiModelProperty(value = "手机号", name = "mobile")
    private String     mobile;
    @ApiModelProperty(value = "验证码", name = "keyCode")
    private String     keyCode;

    @ApiModelProperty(value = "提现类型 1银行卡  2微信余额 3贝宝余额", name = "withdrawStatus")
    private Integer withdrawStatus;

    @ApiModelProperty(value = "贝宝邮箱", name = "email")
    private String email;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getWithdrawStatus()
    {
        return withdrawStatus;
    }

    public void setWithdrawStatus(Integer withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Integer getBankId()
    {
        return bankId;
    }

    public void setBankId(Integer bankId)
    {
        this.bankId = bankId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
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

}
