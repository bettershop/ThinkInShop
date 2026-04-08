package com.laiketui.domain.vo.auction;

import io.swagger.annotations.ApiModelProperty;

/**
 * 竞拍添加商品 - json参数 [{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]
 *
 * @author Trick
 * @date 2022/7/19 14:01
 */
public class AddGoodsVo
{
    @ApiModelProperty(name = "id", value = "竞拍商品活动id")
    private Integer id;
    @ApiModelProperty(name = "attrId", value = "商品规格id")
    private Integer attrId;
    @ApiModelProperty(name = "startingAmt", value = "起拍价")
    private String  startingAmt;
    @ApiModelProperty(name = "markUpAmt", value = "加价幅度")
    private String  markUpAmt;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    public String getStartingAmt()
    {
        return startingAmt;
    }

    public void setStartingAmt(String startingAmt)
    {
        this.startingAmt = startingAmt;
    }

    public String getMarkUpAmt()
    {
        return markUpAmt;
    }

    public void setMarkUpAmt(String markUpAmt)
    {
        this.markUpAmt = markUpAmt;
    }
}
