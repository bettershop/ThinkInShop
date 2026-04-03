package com.laiketui.domain.vo.pay;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;

public class BindStripeEmailVo extends MainVo
{


    /**
     * Stripe注册邮箱
     */
    @ParamsMapping({"stripe_email"})
    private String stripeEmail;


    public String getStripeEmail()
    {
        return stripeEmail;
    }

    public void setStripeEmail(String stripeEmail)
    {
        this.stripeEmail = stripeEmail;
    }


}
