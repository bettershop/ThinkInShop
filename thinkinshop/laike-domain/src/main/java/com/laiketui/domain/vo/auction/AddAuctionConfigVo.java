package com.laiketui.domain.vo.auction;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑竞拍配置
 *
 * @author Trick
 * @date 2021/5/17 16:37
 */
@ApiModel(description = "添加/编辑竞拍配置")
public class AddAuctionConfigVo extends MainVo
{
    @ApiModelProperty(name = "isOpen", value = "是否开启插件 0-不开启 1-开启")
    @ParamsMapping("is_open")
    private Integer isOpen;
    @ApiModelProperty(name = "content", value = "规则")
    private String  content;
    @ApiModelProperty(name = "days", value = "保留天数")
    private Integer days;
    @ApiModelProperty(name = "waitTime", value = "出价等待时间(单位秒)")
    @ParamsMapping("wait_time")
    private Integer waitTime;
    @ApiModelProperty(name = "lowPepole", value = "最低开拍人数")
    @ParamsMapping("low_pepole")
    private Integer lowPepole;

    @ApiModelProperty(name = "orderFailure", value = "订单失效 (单位 小时)")
    private Long orderFailure;
    @ApiModelProperty(name = "autoTime", value = "自动收货时间 (单位 天)")
    private Long autoTime;
    @ApiModelProperty(name = "deliverRemindTimeDay", hidden = true, value = "发货提醒设置 (单位 天)")
    private Long deliverRemindTimeDay;
    @ApiModelProperty(name = "deliverRemindTime", hidden = true, value = "发货提醒设置 (单位 小时)")
    private Long deliverRemindTime;

    @ApiModelProperty(name = "agreeContent", value = "竞拍协议")
    private String agreeContent;
    @ApiModelProperty(name = "agreeTitle", value = "协议标题")
    private String agreeTitle;

    public String getAgreeContent()
    {
        return agreeContent;
    }

    public void setAgreeContent(String agreeContent)
    {
        this.agreeContent = agreeContent;
    }

    public String getAgreeTitle()
    {
        return agreeTitle;
    }

    public void setAgreeTitle(String agreeTitle)
    {
        this.agreeTitle = agreeTitle;
    }

    public Long getDeliverRemindTimeDay()
    {
        return deliverRemindTimeDay;
    }

    public void setDeliverRemindTimeDay(Long deliverRemindTimeDay)
    {
        this.deliverRemindTimeDay = deliverRemindTimeDay;
    }

    public Long getOrderFailure()
    {
        return orderFailure;
    }

    public void setOrderFailure(Long orderFailure)
    {
        this.orderFailure = orderFailure;
    }

    public Long getAutoTime()
    {
        return autoTime;
    }

    public void setAutoTime(Long autoTime)
    {
        this.autoTime = autoTime;
    }

    public Long getDeliverRemindTime()
    {
        return deliverRemindTime;
    }

    public void setDeliverRemindTime(Long deliverRemindTime)
    {
        this.deliverRemindTime = deliverRemindTime;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }

    public Integer getWaitTime()
    {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime)
    {
        this.waitTime = waitTime;
    }

    public Integer getLowPepole()
    {
        return lowPepole;
    }

    public void setLowPepole(Integer lowPepole)
    {
        this.lowPepole = lowPepole;
    }
}
