package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取列表数据
 *
 * @author sunH_
 * @date 2022/9/15 12:03
 */
@ApiModel(description = "获取列表数据")
public class GoodsQueryVo extends MainVo
{

    @ApiModelProperty(value = "商品id", name = "goodId")
    private Integer goodId;

    @ApiModelProperty(value = "多条件", name = "condition")
    private String condition;

    @ApiModelProperty(value = "分类id", name = "cid")
    private Integer cid;

    @ApiModelProperty(value = "商品名称", name = "productTitle")
    private String productTitle;

    @ApiModelProperty(value = "品牌id", name = "brandId")
    private Integer brandId;

    @ApiModelProperty(value = "状态 1:待上架 2:上架 3:下架 4.违规下架 5.断供", name = "status")
    private Integer status;

    @ApiModelProperty(value = "状态集合(1,2,3)", name = "status")
    private String statusList;

    @ApiModelProperty(value = "审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核", name = "mchStatus")
    private Integer mchStatus;

    @ApiModelProperty(value = "审核状态集合(1,2,3)", name = "mchStatus")
    private String mchStatusList;

    @ApiModelProperty(value = "供应商名称", name = "supplierName")
    private String supplierName;

    @ApiModelProperty(value = "开始时间", name = "startTime")
    private String startTime;

    @ApiModelProperty(value = "结束时间", name = "endTime")
    private String endTime;

    @ApiModelProperty(value = "请求页面 1.供应商端商品列表", name = "endTime")
    private Integer requestPage;

    @ApiModelProperty(value = "店铺供应商商品搜索条件", name = "supplierKey")
    private String supplierKey;

    @ApiModelProperty(value = "店铺供应商商品搜索条件", name = "supplierStatus")
    private String supplierStatus;

    public String getSupplierStatus()
    {
        return supplierStatus;
    }

    public void setSupplierStatus(String supplierStatus)
    {
        this.supplierStatus = supplierStatus;
    }

    public String getSupplierKey()
    {
        return supplierKey;
    }

    public void setSupplierKey(String supplierKey)
    {
        this.supplierKey = supplierKey;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public String getStatusList()
    {
        return statusList;
    }

    public void setStatusList(String statusList)
    {
        this.statusList = statusList;
    }

    public Integer getGoodId()
    {
        return goodId;
    }

    public void setGoodId(Integer goodId)
    {
        this.goodId = goodId;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public Integer getBrandId()
    {
        return brandId;
    }

    public void setBrandId(Integer brandId)
    {
        this.brandId = brandId;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getMchStatus()
    {
        return mchStatus;
    }

    public void setMchStatus(Integer mchStatus)
    {
        this.mchStatus = mchStatus;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public String getMchStatusList()
    {
        return mchStatusList;
    }

    public void setMchStatusList(String mchStatusList)
    {
        this.mchStatusList = mchStatusList;
    }

    public Integer getRequestPage()
    {
        return requestPage;
    }

    public void setRequestPage(Integer requestPage)
    {
        this.requestPage = requestPage;
    }
}
