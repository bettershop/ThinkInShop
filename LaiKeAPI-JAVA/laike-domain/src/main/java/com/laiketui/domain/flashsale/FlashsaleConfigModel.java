package com.laiketui.domain.flashsale;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 限时折扣设置
 */
@Table(name = "lkt_flashsale_config")
public class FlashsaleConfigModel implements Serializable
{
    /**
     * 主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer storeId;

    /**
     * 是否开启 1 是 0 否
     */
    private Integer isOpen;

    /**
     * 订单售后时间 (单位秒)
     */
    private Integer orderAfter;

    /**
     * 自动收货时间
     */
    private Integer autoTheGoods;

    /**
     * 自动评价设置几天后自动好评
     */
    private Integer autoGoodCommentDay;

    /**
     * 好评内容
     */
    private String autoGoodCommentContent;

    /**
     * 店铺id
     */
    private Integer mchId;

    /**
     * 自动评价设置 0.关闭 1.开启
     */
    private Integer goodSwitch;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public Integer getOrderAfter()
    {
        return orderAfter;
    }

    public void setOrderAfter(Integer orderAfter)
    {
        this.orderAfter = orderAfter;
    }

    public Integer getAutoTheGoods()
    {
        return autoTheGoods;
    }

    public void setAutoTheGoods(Integer autoTheGoods)
    {
        this.autoTheGoods = autoTheGoods;
    }

    public Integer getAutoGoodCommentDay()
    {
        return autoGoodCommentDay;
    }

    public void setAutoGoodCommentDay(Integer autoGoodCommentDay)
    {
        this.autoGoodCommentDay = autoGoodCommentDay;
    }

    public String getAutoGoodCommentContent()
    {
        return autoGoodCommentContent;
    }

    public void setAutoGoodCommentContent(String autoGoodCommentContent)
    {
        this.autoGoodCommentContent = autoGoodCommentContent;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public Integer getGoodSwitch()
    {
        return goodSwitch;
    }

    public void setGoodSwitch(Integer goodSwitch)
    {
        this.goodSwitch = goodSwitch;
    }
}
