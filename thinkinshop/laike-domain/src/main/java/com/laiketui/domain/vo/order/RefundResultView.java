package com.laiketui.domain.vo.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 售后记录回显
 *
 * @author Trick
 * @date 2023/3/7 20:41
 */
@ApiModel(description = "售后记录回显")
public class RefundResultView
{

    @ApiModelProperty(value = "申请状态")
    private String     examineResult;
    @ApiModelProperty(value = "申请时间")
    private String     applyTime;
    @ApiModelProperty(value = "审核时间")
    private String     examineResultTime;
    @ApiModelProperty(value = "回寄物流")
    private String     reBackNo;
    @ApiModelProperty(value = "回寄时间")
    private String     reBackTime;
    @ApiModelProperty(value = "退货物流")
    private String     returnBackNo;
    @ApiModelProperty(value = "拒绝原因")
    private String     refuseText;
    @ApiModelProperty(value = "退款金额")
    private BigDecimal returnMoney;

    public String getExamineResult()
    {
        return examineResult;
    }

    public void setExamineResult(String examineResult)
    {
        this.examineResult = examineResult;
    }

    public String getApplyTime()
    {
        return applyTime;
    }

    public void setApplyTime(String applyTime)
    {
        this.applyTime = applyTime;
    }

    public String getExamineResultTime()
    {
        return examineResultTime;
    }

    public void setExamineResultTime(String examineResultTime)
    {
        this.examineResultTime = examineResultTime;
    }

    public String getReBackNo()
    {
        return reBackNo;
    }

    public void setReBackNo(String reBackNo)
    {
        this.reBackNo = reBackNo;
    }

    public String getReBackTime()
    {
        return reBackTime;
    }

    public void setReBackTime(String reBackTime)
    {
        this.reBackTime = reBackTime;
    }

    public String getReturnBackNo()
    {
        return returnBackNo;
    }

    public void setReturnBackNo(String returnBackNo)
    {
        this.returnBackNo = returnBackNo;
    }

    public String getRefuseText()
    {
        return refuseText;
    }

    public void setRefuseText(String refuseText)
    {
        this.refuseText = refuseText;
    }

    public BigDecimal getReturnMoney()
    {
        return returnMoney;
    }

    public void setReturnMoney(BigDecimal returnMoney)
    {
        this.returnMoney = returnMoney;
    }
}
