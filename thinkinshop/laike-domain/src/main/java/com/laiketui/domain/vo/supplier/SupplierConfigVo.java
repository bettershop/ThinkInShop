package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/9/14
 */
@ApiModel(description = "添加修改供应商配置信息")
public class SupplierConfigVo extends MainVo
{

    @ApiModelProperty(value = "最小充值金额", name = "minRecharge")
    private BigDecimal minRecharge;
    @ApiModelProperty(value = "最小提现金额", name = "minWithdrawal")
    private BigDecimal minWithdrawal;
    @ApiModelProperty(value = "最大提现金额", name = "maxWithdrawal")
    private BigDecimal maxWithdrawal;
    @ApiModelProperty(value = "手续费", name = "commission")
    private BigDecimal commission;
    @ApiModelProperty(value = "钱包单位", name = "walletUnit")
    private String     walletUnit;
    @ApiModelProperty(value = "提现说明", name = "withdrawalIns")
    private String     withdrawalIns;

    public String getWithdrawalIns()
    {
        return withdrawalIns;
    }

    public void setWithdrawalIns(String withdrawalIns)
    {
        this.withdrawalIns = withdrawalIns;
    }

    public BigDecimal getMinRecharge()
    {
        return minRecharge;
    }

    public void setMinRecharge(BigDecimal minRecharge)
    {
        this.minRecharge = minRecharge;
    }

    public BigDecimal getMinWithdrawal()
    {
        return minWithdrawal;
    }

    public void setMinWithdrawal(BigDecimal minWithdrawal)
    {
        this.minWithdrawal = minWithdrawal;
    }

    public BigDecimal getMaxWithdrawal()
    {
        return maxWithdrawal;
    }

    public void setMaxWithdrawal(BigDecimal maxWithdrawal)
    {
        this.maxWithdrawal = maxWithdrawal;
    }

    public BigDecimal getCommission()
    {
        return commission;
    }

    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    public String getWalletUnit()
    {
        return walletUnit;
    }

    public void setWalletUnit(String walletUnit)
    {
        this.walletUnit = walletUnit;
    }
}
