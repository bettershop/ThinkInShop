package com.laiketui.domain.vo.freight;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 16:58 2022/12/1
 */
public class FreightRuleVO
{
    @ApiModelProperty(name = "one", value = "首件首重")
    private String one;

    @ApiModelProperty(name = "num6", value = "首费")
    private String freight;

    @ApiModelProperty(name = "two", value = "续件续重")
    private String two;

    @JsonProperty
    @ApiModelProperty(name = "Tfreight", value = "续费")
    private String Tfreight;

    @ApiModelProperty(name = "picList", value = "省级")//湖南省,江苏省,江西省
    private String picList;

    @ApiModelProperty(name = "cid", value = "省级id")//1,2,3
    private String cid;

    @ApiModelProperty(name = "name", value = "省市区三级")//湖南省-长沙市-岳麓区,湖南省-长沙市-雨花区,湖南省-长沙市-望城区
    private String name;

    @ApiModelProperty(name = "canshuID", value = "pc端编辑运费回显运费规则地区")//湖南省-长沙市-岳麓区,湖南省-长沙市-雨花区,湖南省-长沙市-望城区
    private String canshuID;

    public String getOne()
    {
        return one;
    }

    public void setOne(String one)
    {
        this.one = one;
    }

    public String getFreight()
    {
        return freight;
    }

    public void setFreight(String freight)
    {
        this.freight = freight;
    }

    public String getTwo()
    {
        return two;
    }

    public void setTwo(String two)
    {
        this.two = two;
    }

    @JsonIgnore
    public String getTfreight()
    {
        return Tfreight;
    }

    @JsonIgnore
    public void setTfreight(String tfreight)
    {
        Tfreight = tfreight;
    }

    public String getPicList()
    {
        return picList;
    }

    public void setPicList(String picList)
    {
        this.picList = picList;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getCanshuID()
    {
        return canshuID;
    }

    public void setCanshuID(String canshuID)
    {
        this.canshuID = canshuID;
    }
}
