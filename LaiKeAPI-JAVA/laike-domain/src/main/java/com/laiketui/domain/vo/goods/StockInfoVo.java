package com.laiketui.domain.vo.goods;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 库存信息参数
 *
 * @author Trick
 * @date 2021/1/4 10:49
 */
@ApiModel(description = "库存信息参数")
public class StockInfoVo extends MainVo
{
    @ApiModelProperty(value = "商品编码", name = "product_number")
    private String  productNumber;
    @ApiModelProperty(value = "店铺名称", name = "mch_name")
    private String  mchName;
    @ApiModelProperty(value = "商品名称", name = "product_title")
    private String  productTitle;
    @ApiModelProperty(value = "状态 1.在售 2.断供", name = "status")
    private Integer status;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String  endDate;
    @ApiModelProperty("排序字段")
    private String sortCriteria;
    @ApiModelProperty("排序")
    private String sort;

    public Integer getStatus()
    {
        return status;
    }

    public String getSortCriteria()
    {
        return sortCriteria;
    }

    public void setSortCriteria(String sortCriteria)
    {
        this.sortCriteria = sortCriteria;
    }

    public String getSort()
    {
        return sort;
    }

    public void setSort(String sort)
    {
        this.sort = sort;
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

    public String getProductNumber()
    {
        return productNumber;
    }

    public void setProductNumber(String productNumber)
    {
        this.productNumber = productNumber;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

}
