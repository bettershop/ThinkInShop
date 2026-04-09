package com.laiketui.domain.vo.order;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取发货记录列表
 */
public class GetExpressDeliveryListVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "ID")
    private Integer id;

    @ApiModelProperty(name = "expressName", value = "快递单号、快递订单ID")
    private String expressName;

    @ApiModelProperty(name = "sNo", value = "订单号")
    private String sNo;

    @ApiModelProperty(name = "mch_name", value = "店铺名称")
    private String mch_name;

    @ApiModelProperty(name = "search", value = "快递单号、订单号")
    private String search;

    @ApiModelProperty(name = "status", value = "是否打印 0.未打印 1.已打印")
    private Integer status;

    @ApiModelProperty(name = "startDate", value = "查询开始时间")
    private String startDate;

    @ApiModelProperty(name = "endDate", value = "查询结束时间")
    private String endDate;

    @ApiModelProperty(name = "mch_id", value = "店铺id")
    private Integer mch_id;


    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getExpressName()
    {
        return expressName;
    }

    public void setExpressName(String expressName)
    {
        this.expressName = expressName;
    }

    public String getMch_name()
    {
        return mch_name;
    }

    public String getSearch()
    {
        return search;
    }

    public void setSearch(String search)
    {
        this.search = search;
    }

    public void setMch_name(String mch_name)
    {
        this.mch_name = mch_name;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
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
}
