package com.laiketui.domain.vo.plugin.presell;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 添加/编辑秒杀配置信息
 *
 * @author sunH_
 * @date 2021/12/28 16:51
 */
@ApiModel(description = "预售配置信息")
public class PreSellConfigVO extends MainVo
{
    @ApiModelProperty(value = "是否开启预售 1是 0否")
    private Integer    isOpen;
    @ApiModelProperty(value = "用户端入口是否展示 0.否 1.是")
    private Integer    userEntrance;
    @ApiModelProperty(value = "定金预售说明")
    private String     depositDesc;
    @ApiModelProperty(value = "订货预售说明")
    private String     balanceDesc;
    @ApiModelProperty(name = "isFreeShipping", value = "是否开启包邮设置 0.否 1.是")
    private Integer    isFreeShipping;
    @ApiModelProperty(name = "goodsNum", value = "满足包邮条件的商品数量")
    private Integer    goodsNum;
    @ApiModelProperty(name = "autoReceivingGoodsDay", value = "自动收货时间 (天)")
    private BigDecimal autoReceivingGoodsDay;
    @ApiModelProperty(name = "orderInvalidTime", value = "订单失效时间 (小时)")
    private BigDecimal orderInvalidTime;
    @ApiModelProperty(name = "returnDay", value = "订单售后时间 (天)")
    private BigDecimal returnDay;
    @ApiModelProperty(name = "deliverRemind", value = "提醒限制 (天)")
    private BigDecimal deliverRemind;
    @ApiModelProperty(name = "autoCommentDay", value = "自动好评时间 (天)")
    private BigDecimal autoCommentDay;
    @ApiModelProperty(name = "autoCommentContent", value = "自动评价内容")
    private String     autoCommentContent;

    public Integer getUserEntrance()
    {
        return userEntrance;
    }

    public void setUserEntrance(Integer userEntrance)
    {
        this.userEntrance = userEntrance;
    }

    public String getAutoCommentContent()
    {
        return autoCommentContent;
    }

    public void setAutoCommentContent(String autoCommentContent)
    {
        this.autoCommentContent = autoCommentContent;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public String getDepositDesc()
    {
        return depositDesc;
    }

    public void setDepositDesc(String depositDesc)
    {
        this.depositDesc = depositDesc;
    }

    public String getBalanceDesc()
    {
        return balanceDesc;
    }

    public void setBalanceDesc(String balanceDesc)
    {
        this.balanceDesc = balanceDesc;
    }

    public Integer getIsFreeShipping()
    {
        return isFreeShipping;
    }

    public void setIsFreeShipping(Integer isFreeShipping)
    {
        this.isFreeShipping = isFreeShipping;
    }

    public Integer getGoodsNum()
    {
        return goodsNum;
    }

    public void setGoodsNum(Integer goodsNum)
    {
        this.goodsNum = goodsNum;
    }

    public BigDecimal getAutoReceivingGoodsDay()
    {
        return autoReceivingGoodsDay;
    }

    public void setAutoReceivingGoodsDay(BigDecimal autoReceivingGoodsDay)
    {
        this.autoReceivingGoodsDay = autoReceivingGoodsDay;
    }

    public BigDecimal getOrderInvalidTime()
    {
        return orderInvalidTime;
    }

    public void setOrderInvalidTime(BigDecimal orderInvalidTime)
    {
        this.orderInvalidTime = orderInvalidTime;
    }

    public BigDecimal getReturnDay()
    {
        return returnDay;
    }

    public void setReturnDay(BigDecimal returnDay)
    {
        this.returnDay = returnDay;
    }

    public BigDecimal getDeliverRemind()
    {
        return deliverRemind;
    }

    public void setDeliverRemind(BigDecimal deliverRemind)
    {
        this.deliverRemind = deliverRemind;
    }

    public BigDecimal getAutoCommentDay()
    {
        return autoCommentDay;
    }

    public void setAutoCommentDay(BigDecimal autoCommentDay)
    {
        this.autoCommentDay = autoCommentDay;
    }
}
