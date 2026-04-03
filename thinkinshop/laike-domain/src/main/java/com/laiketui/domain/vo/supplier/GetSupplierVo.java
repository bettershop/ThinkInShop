package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:34 2022/9/14
 */
@ApiModel(description = "查询供应商信息")
public class GetSupplierVo extends MainVo
{

    @ApiModelProperty(value = "供应商id", name = "supplierId")
    private Integer supplierId;
    @ApiModelProperty(value = "供应商名称", name = "supplierName")
    private String  supplierName;
    @ApiModelProperty(value = "供应商类型id", name = "dicId")
    private Integer dicId;
    @ApiModelProperty(value = "所属性质1.个人 2.企业", name = "type")
    private Integer type;
    @ApiModelProperty(value = "状态 0.正常 1.到期 2.锁定", name = "status")
    private Integer status;
    @ApiModelProperty(value = "开始时间", name = "startTime")
    private String  startTime;
    @ApiModelProperty(value = "结束时间", name = "endTime")
    private String  endTime;

    public Integer getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public Integer getDicId()
    {
        return dicId;
    }

    public void setDicId(Integer dicId)
    {
        this.dicId = dicId;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
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
}
