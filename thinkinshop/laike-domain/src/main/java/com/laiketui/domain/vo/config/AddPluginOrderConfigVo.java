package com.laiketui.domain.vo.config;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加插件订单配置参数
 *
 * @author Trick
 * @date 2023/3/17 15:04
 */
@ApiModel(description = "添加插件订单配置参数")
public class AddPluginOrderConfigVo extends MainVo
{

    @ApiModelProperty(name = "autoTheGoods", value = "自动收货时间 /天")
    private Integer autoTheGoods;
    @ApiModelProperty(name = "orderFailure", value = "订单失效时间 /小时")
    private Integer orderFailure;
    @ApiModelProperty(name = "orderAfter", value = "订单售后时间 /天")
    private Integer orderAfter;
    @ApiModelProperty(name = "autoGoodCommentDay", value = "自动评价设置 /天")
    private Integer autoGoodCommentDay;
    @ApiModelProperty(name = "autoCommentContent", value = "自动评价内容")
    private String  autoCommentContent;
    @ApiModelProperty(name = "content", value = "规则")
    private String  content;

    @ApiModelProperty(name = "pluginCode", value = "插件类型")
    private String pluginCode;

    @ApiModelProperty(name = "agreeContent", value = "竞拍协议")
    private String  agreeContent;
    @ApiModelProperty(name = "agreeTitle", value = "协议标题")
    private String  agreeTitle;
    @ApiModelProperty(name = "configValue", value = "插件订单配置参数类容")
    private String  configValue;
    @ApiModelProperty(name = "isOpen", value = "是否开启拼团插件 插件状态 0关闭 1开启")
    private Integer isOpen;

    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

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

    public String getPluginCode()
    {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode)
    {
        this.pluginCode = pluginCode;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public void setAutoTheGoods(Integer autoTheGoods)
    {
        this.autoTheGoods = autoTheGoods;
    }

    public Integer getAutoTheGoods()
    {
        return autoTheGoods;
    }

    public Integer getOrderFailure()
    {
        return orderFailure;
    }

    public void setOrderFailure(Integer orderFailure)
    {
        this.orderFailure = orderFailure;
    }

    public Integer getOrderAfter()
    {
        return orderAfter;
    }

    public void setOrderAfter(Integer orderAfter)
    {
        this.orderAfter = orderAfter;
    }

    public Integer getAutoGoodCommentDay()
    {
        return autoGoodCommentDay;
    }

    public void setAutoGoodCommentDay(Integer autoGoodCommentDay)
    {
        this.autoGoodCommentDay = autoGoodCommentDay;
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
}
