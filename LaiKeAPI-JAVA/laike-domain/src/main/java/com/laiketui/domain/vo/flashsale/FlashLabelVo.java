package com.laiketui.domain.vo.flashsale;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 添加修改活动
 */
public class FlashLabelVo extends MainVo
{

    @ApiModelProperty(name = "id", value = "活动id")
    private Integer id;

    @ApiModelProperty(name = "goodsJson", value = "商品信息  [{\"attrId\":4,\"discount\":\"3\",\"buylimit\":30},{\"attrId\":5,\"discount\":\"3\",\"buylimit\":30}]")
    private String goodsJson;

    @ApiModelProperty(name = "name", value = "活动name")
    private String name;

    @ApiModelProperty(name = "discount", value = "活动折扣")
    private BigDecimal discount;

    @ApiModelProperty(name = "buylimit", value = "限购数量")
    private Integer buylimit;

    @ApiModelProperty(name = "startDate", value = "开始时间")
    private String startDate;

    @ApiModelProperty(name = "endDate", value = "结束时间")
    private String endDate;

    @ApiModelProperty(name = "content", value = "说明详情")
    private String content;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getGoodsJson()
    {
        return goodsJson;
    }

    public void setGoodsJson(String goodsJson)
    {
        this.goodsJson = goodsJson;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public BigDecimal getDiscount()
    {
        return discount;
    }

    public void setDiscount(BigDecimal discount)
    {
        this.discount = discount;
    }

    public Integer getBuylimit()
    {
        return buylimit;
    }

    public void setBuylimit(Integer buylimit)
    {
        this.buylimit = buylimit;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
