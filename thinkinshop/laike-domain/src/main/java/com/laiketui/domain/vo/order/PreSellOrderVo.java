package com.laiketui.domain.vo.order;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预售订单参数
 *
 * @author sunH_
 * @date 2021/12/22 14:35
 */
@ApiModel(description = "预售订单参数")
public class PreSellOrderVo extends MainVo
{


    @ApiModelProperty(value = "预售类型 1.定金模式 2.订货模式", name = "sellType")
    private Integer sellType;
    @ApiModelProperty(value = "订单状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭", name = "status")
    private Integer status;
    @ApiModelProperty(value = "订单状态集合", name = "statusList")
    private String  statusList;
    @ApiModelProperty(value = "商品编号/商品名称", name = "product")
    private String  product;
    @ApiModelProperty(value = "商品编号", name = "productId")
    private Integer productId;
    @ApiModelProperty(value = "开始日期", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束日期", name = "endDate")
    private String  endDate;
    @ApiModelProperty(value = "是否查询退款成功订单 0.否 1.是", name = "isRefund")
    private String  isRefund;
    @ApiModelProperty(value = "条件(预售商品传)", name = "condition")
    private String  keyWord;
    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String  mchName;

    public Integer getProductId()
    {
        return productId;
    }

    public void setProductId(Integer productId)
    {
        this.productId = productId;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getStatusList()
    {
        return statusList;
    }

    public void setStatusList(String statusList)
    {
        this.statusList = statusList;
    }

    public String getProduct()
    {
        return product;
    }

    public void setProduct(String product)
    {
        this.product = product;
    }

    public String getKeyWord()
    {
        return keyWord;
    }

    public void setKeyWord(String keyWord)
    {
        this.keyWord = keyWord;
    }

    public Integer getSellType()
    {
        return sellType;
    }

    public void setSellType(Integer sellType)
    {
        this.sellType = sellType;
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

    public String getIsRefund()
    {
        return isRefund;
    }

    public void setIsRefund(String isRefund)
    {
        this.isRefund = isRefund;
    }
}
