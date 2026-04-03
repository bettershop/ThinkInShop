package com.laiketui.domain.vo.admin.role;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取管理员日志参数
 *
 * @author Trick
 * @date 2021/1/13 17:20
 */
@ApiModel(description = "获取管理员日志参数")
public class LoggerAdminVo extends MainVo
{
    @ApiModelProperty(value = "日志id", name = "logId")
    private Integer logId;
    @ApiModelProperty(value = "管理员名称", name = "adminName")
    private String  adminName;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String  endDate;

    //新加参数
    @ApiModelProperty(value = "日志账号类型", name = "logAccountType")
    private String logAccountType;

    @ApiModelProperty(value = "日志操作类型", name = "logOperationType")
    private String logOperationType;

    public String getLogAccountType()
    {
        return logAccountType;
    }

    public void setLogAccountType(String logAccountType)
    {
        this.logAccountType = logAccountType;
    }

    public String getLogOperationType()
    {
        return logOperationType;
    }

    public void setLogOperationType(String logOperationType)
    {
        this.logOperationType = logOperationType;
    }

    public Integer getLogId()
    {
        return logId;
    }

    public void setLogId(Integer logId)
    {
        this.logId = logId;
    }

    public String getAdminName()
    {
        return adminName;
    }

    public void setAdminName(String adminName)
    {
        this.adminName = adminName;
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
