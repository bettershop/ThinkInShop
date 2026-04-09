package com.laiketui.domain.vo.pc;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.servlet.http.HttpServletResponse;

/**
 * 订单退款列表
 *
 * @author Trick
 * @date 2020/11/24 17:07
 */
@ApiModel("订单退款列表")
public class MchPcReturnOrderVo extends MainVo
{

    @ApiModelProperty(value = "店铺id", name = "shopId")
    @ParamsMapping("shop_id")
    private int     shopId;
    @ApiModelProperty(value = "售后状态 0=待审核 1=退款中 2=退款成功 3=退款失败 4=换货中 5=换货成功 6=换货失败", name = "orderStauts")
    @ParamsMapping("status")
    private Integer orderStauts;
    @ApiModelProperty(value = "售后类型", name = "reType")
    private Integer reType;
    @ApiModelProperty(value = "订单号/姓名/会员/电话号", name = "orderno")
    private String  orderno;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String  endDate;
    @ApiModelProperty(value = "来源", name = "source")
    public  Integer source;

    public Integer getSource()
    {
        return source;
    }

    public void setSource(Integer source)
    {
        this.source = source;
    }


    private HttpServletResponse response;

    public HttpServletResponse getResponse()
    {
        return response;
    }


    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }

    public int getShopId()
    {
        return shopId;
    }

    public void setShopId(int shopId)
    {
        this.shopId = shopId;
    }

    public Integer getOrderStauts()
    {
        return orderStauts;
    }

    public void setOrderStauts(Integer orderStauts)
    {
        this.orderStauts = orderStauts;
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

    public Integer getReType()
    {
        return reType;
    }

    public void setReType(Integer reType)
    {
        this.reType = reType;
    }
}
