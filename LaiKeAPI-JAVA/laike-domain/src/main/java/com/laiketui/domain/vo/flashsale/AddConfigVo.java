package com.laiketui.domain.vo.flashsale;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

public class AddConfigVo extends MainVo
{
    @ApiModelProperty(name = "is_open", value = "是否开启")
    private Integer is_open;
    @ApiModelProperty(name = "auto_the_goods", value = "自动收货时间")
    private Integer auto_the_goods;
    @ApiModelProperty(name = "order_after", value = "订单售后时间 (单位秒)")
    private Integer order_after;
    @ApiModelProperty(name = "goodSwitch", value = "自动评价设置 0.关闭 1.开启")
    private Integer goodSwitch;
    @ApiModelProperty(name = "auto_good_comment_day", value = "自动评价设置几后自动好评")
    private Integer auto_good_comment_day;
    @ApiModelProperty(name = "auto_good_comment_content", value = "好评内容")
    private String  auto_good_comment_content;

    public Integer getIs_open()
    {
        return is_open;
    }

    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }

    public Integer getAuto_the_goods()
    {
        return auto_the_goods;
    }

    public void setAuto_the_goods(Integer auto_the_goods)
    {
        this.auto_the_goods = auto_the_goods;
    }

    public Integer getOrder_after()
    {
        return order_after;
    }

    public void setOrder_after(Integer order_after)
    {
        this.order_after = order_after;
    }

    public Integer getGoodSwitch()
    {
        return goodSwitch;
    }

    public void setGoodSwitch(Integer goodSwitch)
    {
        this.goodSwitch = goodSwitch;
    }

    public Integer getAuto_good_comment_day()
    {
        return auto_good_comment_day;
    }

    public void setAuto_good_comment_day(Integer auto_good_comment_day)
    {
        this.auto_good_comment_day = auto_good_comment_day;
    }

    public String getAuto_good_comment_content()
    {
        return auto_good_comment_content;
    }

    public void setAuto_good_comment_content(String auto_good_comment_content)
    {
        this.auto_good_comment_content = auto_good_comment_content;
    }
}
