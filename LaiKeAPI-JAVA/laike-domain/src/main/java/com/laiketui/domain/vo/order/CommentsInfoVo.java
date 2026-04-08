package com.laiketui.domain.vo.order;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This is description of java
 *
 * @author Trick
 * @date 2021/1/6 16:13
 */
@ApiModel(description = "售后查询参数")
public class CommentsInfoVo extends MainVo
{

    @ApiModelProperty(value = "评论id", name = "cid")
    private Integer cid;
    @ApiModelProperty(value = "评论级别 GOOD=好评,NOTBAD=中评,BAD=差评,HAVEIMG=有图", name = "type")
    private String  type;
    @ApiModelProperty(value = "订单号", name = "orderno")
    private String  orderno;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String  endDate;

    @ApiModelProperty(value = "订单类型", name = "orderType", hidden = true)
    private String  orderType;
    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String  mchName;
    @ApiModelProperty(value = "订单详情id", name = "detailId")
    private Integer detailId;
    @ApiModelProperty(value = "用户id/用户名称", name = "userIdOrName")
    private String  userIdOrName;
    @ApiModelProperty(value = "商品id", name = "productId")
    private String  productId;

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public String getUserIdOrName()
    {
        return userIdOrName;
    }

    public void setUserIdOrName(String userIdOrName)
    {
        this.userIdOrName = userIdOrName;
    }

    public Integer getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Integer detailId)
    {
        this.detailId = detailId;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getOrderno()
    {
        return orderno;
    }

    public void setOrderno(String orderno)
    {
        this.orderno = orderno;
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
