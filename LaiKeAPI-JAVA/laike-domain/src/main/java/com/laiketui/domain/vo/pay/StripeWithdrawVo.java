package com.laiketui.domain.vo.pay;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;

import java.math.BigDecimal;

public class StripeWithdrawVo extends MainVo
{
    /**
     * Stripe账户ID
     */
    @ParamsMapping({"stripe_account_id"})
    private String stripeAccountId; // Stripe账户ID

    /**
     * 提现金额，单位为分
     */
    @ParamsMapping({"amount"})
    private BigDecimal amount; // 提现金额，单位为分

    /**
     * 货币类型，例如 "usd" 或 "eur" 等
     */
    @ParamsMapping({"currency"})
    private String currency; // 货币类型，例如 "usd"

    public String getStripeAccountId()
    {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId)
    {
        this.stripeAccountId = stripeAccountId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }
}
