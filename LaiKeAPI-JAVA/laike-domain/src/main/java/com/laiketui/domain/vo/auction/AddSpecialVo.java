package com.laiketui.domain.vo.auction;


import com.laiketui.domain.auction.AuctionSpecialModel;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 添加专场参数
 *
 * @author Trick
 * @date 2022/7/1 15:30
 */
@ApiModel(description = "添加/编辑 专场参数")
public class AddSpecialVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "竞拍专场id")
    private String     id;
    @ApiModelProperty(name = "title", value = "专场名称")
    private String     title;
    @ApiModelProperty(name = "type", value = "专场类型 1=店铺专场 2=普通专场 3=报名专场 默认店铺专场")
    private Integer    type = AuctionSpecialModel.SpecialType.TYPE_MCH;
    @ApiModelProperty(name = "commissionAmt", value = "专场佣金")
    private BigDecimal commissionAmt;
    @ApiModelProperty(name = "promiseAmt", value = "专场保证金")
    private BigDecimal promiseAmt;
    @ApiModelProperty(name = "signEndDate", value = "报名截至时间")
    private String     signEndDate;
    @ApiModelProperty(name = "startDate", value = "开始时间")
    private String     startDate;
    @ApiModelProperty(name = "endDate", value = "结束时间")
    private String     endDate;

    @ApiModelProperty(name = "img", value = "专场图片")
    private String img;
    @ApiModelProperty(name = "content", value = "专场预告")
    private String content;

    @ApiModelProperty(name = "unit", value = "起拍价默认单位 1=固定金额 2=价格百分比")
    private Integer    unit;
    @ApiModelProperty(name = "startAmt", value = "默认起拍价")
    private BigDecimal startAmt;
    @ApiModelProperty(name = "markUpAmt", value = "默认加价幅度")
    private BigDecimal markUpAmt;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public BigDecimal getStartAmt()
    {
        return startAmt;
    }

    public void setStartAmt(BigDecimal startAmt)
    {
        this.startAmt = startAmt;
    }

    public BigDecimal getMarkUpAmt()
    {
        return markUpAmt;
    }

    public void setMarkUpAmt(BigDecimal markUpAmt)
    {
        this.markUpAmt = markUpAmt;
    }

    public Integer getUnit()
    {
        return unit;
    }

    public void setUnit(Integer unit)
    {
        this.unit = unit;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public BigDecimal getPromiseAmt()
    {
        return promiseAmt;
    }

    public BigDecimal getCommissionAmt()
    {
        return commissionAmt;
    }

    public void setCommissionAmt(BigDecimal commissionAmt)
    {
        this.commissionAmt = commissionAmt;
    }

    public void setPromiseAmt(BigDecimal promiseAmt)
    {
        this.promiseAmt = promiseAmt;
    }

    public String getSignEndDate()
    {
        return signEndDate;
    }

    public void setSignEndDate(String signEndDate)
    {
        this.signEndDate = signEndDate;
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

    public String getImg()
    {
        return img;
    }

    public void setImg(String img)
    {
        this.img = img;
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
