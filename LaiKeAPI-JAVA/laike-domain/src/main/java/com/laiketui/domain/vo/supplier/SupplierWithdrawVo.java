package com.laiketui.domain.vo.supplier;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:34 2022/9/14
 */
@ApiModel(description = "查询供应商提现信息")
public class SupplierWithdrawVo extends MainVo
{

    @ApiModelProperty(value = "供应商名称", name = "supplierName")
    private String  supplierName;
    @ApiModelProperty(value = "联系电话", name = "phone")
    private String  phone;
    @ApiModelProperty(value = "状态 0：审核中 1：审核通过 2：拒绝", name = "status")
    private String  status;
    @ApiModelProperty(value = "开始时间", name = "startTime")
    private String  startTime;
    @ApiModelProperty(value = "结束时间", name = "endTime")
    private String  endTime;
    @ApiModelProperty(value = "导出数据 1.提现审核 2.提现记录", name = "exportData")
    private Integer exportData;

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
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

    public Integer getExportData()
    {
        return exportData;
    }

    public void setExportData(Integer exportData)
    {
        this.exportData = exportData;
    }
}
