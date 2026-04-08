package com.laiketui.domain.vo.plugin.auction;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 竞拍保证金订单参数
 *
 * @author Trick
 * @date 2022/7/18 11:01
 */
public class PromiseOrderVo
{
    @ApiModelProperty(name = "storeId", value = "商城id")
    private Integer    storeId;
    @ApiModelProperty(name = "paymentAmt", value = "订单金额")
    private BigDecimal paymentAmt;
    @ApiModelProperty(name = "user_id", value = "用户id")
    private String     user_id;
    @ApiModelProperty(name = "pay", value = "支付方式")
    private String     pay;
    @ApiModelProperty(name = "specialId", value = "场次id")
    private String     specialId;

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public BigDecimal getPaymentAmt()
    {
        return paymentAmt;
    }

    public void setPaymentAmt(BigDecimal paymentAmt)
    {
        this.paymentAmt = paymentAmt;
    }

    public String getUser_id()
    {
        return user_id;
    }

    public void setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    public String getPay()
    {
        return pay;
    }

    public void setPay(String pay)
    {
        this.pay = pay;
    }

    public String getSpecialId()
    {
        return specialId;
    }

    public void setSpecialId(String specialId)
    {
        this.specialId = specialId;
    }
}
