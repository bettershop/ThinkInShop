package com.laiketui.domain.vo.coupon;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.servlet.http.HttpServletResponse;

/**
 * 获取店铺优惠卷列表参数
 *
 * @author Trick
 * @date 2021/6/9 15:58
 */
@ApiModel(description = "获取店铺优惠卷列表参数")
public class CouponParmaVo extends MainVo
{
    @ApiModelProperty(value = "活动Id", name = "hid")
    private Integer hid;
    @ApiModelProperty(value = "店铺Id", name = "mchId")
    @ParamsMapping("mch_id")
    private Integer mchId;
    @ApiModelProperty(value = "优惠卷名称", name = "name")
    private String  name;
    @ApiModelProperty(value = "优惠卷类型 1=免邮券，2=满减券，3=折扣券，4=会员赠券", name = "activityType")
    private Integer activityType;
    @ApiModelProperty(value = "优惠卷状态 状态 0：未启用 1：启用 2:禁用 3：已结束", name = "status")
    private Integer status;
    @ApiModelProperty(value = "是否过期 0=过期 1=未过期", name = "isOverdue")
    private Integer isOverdue;

    @ApiModelProperty(value = "发行单位 0=商城 1=店铺", name = "issueUnit")
    private Integer issueUnit;
    @ApiModelProperty(value = "领取方式 0=手动领取 1=系统赠送", name = "receiveType")
    private Integer receiveType;

    private HttpServletResponse response;

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }

    public Integer getIssueUnit()
    {
        return issueUnit;
    }

    public void setIssueUnit(Integer issueUnit)
    {
        this.issueUnit = issueUnit;
    }

    public Integer getReceiveType()
    {
        return receiveType;
    }

    public void setReceiveType(Integer receiveType)
    {
        this.receiveType = receiveType;
    }

    public Integer getHid()
    {
        return hid;
    }

    public void setHid(Integer hid)
    {
        this.hid = hid;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getActivityType()
    {
        return activityType;
    }

    public void setActivityType(Integer activityType)
    {
        this.activityType = activityType;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getIsOverdue()
    {
        return isOverdue;
    }

    public void setIsOverdue(Integer isOverdue)
    {
        this.isOverdue = isOverdue;
    }
}
