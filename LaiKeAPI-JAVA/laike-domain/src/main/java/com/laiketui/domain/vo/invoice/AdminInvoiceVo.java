package com.laiketui.domain.vo.invoice;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/7/28
 */
@ApiModel(description = "发票信息管理")
public class AdminInvoiceVo extends MainVo
{

    @ApiModelProperty(value = "多条件查询", name = "condition")
    private String  condition;
    @ApiModelProperty(value = "开票状态 1.申请中 2.已完成 3.已撤销", name = "companyName")
    private Integer invoiceStatus;
    @ApiModelProperty(value = "抬头类型 1.企业  2.个人", name = "companyTaxNumber")
    private Integer type;

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Integer getInvoiceStatus()
    {
        return invoiceStatus;
    }

    public void setInvoiceStatus(Integer invoiceStatus)
    {
        this.invoiceStatus = invoiceStatus;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }
}
