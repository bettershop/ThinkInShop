package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 提现管理查询参数
 *
 * @author Trick
 * @date 2021/1/11 14:07
 */
@ApiModel(description = "提现管理查询参数")
public class WithdrawalVo extends MainVo
{
    @ApiModelProperty(value = "提现Id", name = "wid")
    private Integer wid;

    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String mchName;
    @ApiModelProperty(value = "会员名称", name = "userName")
    private String userName;
    @ApiModelProperty(value = "联系电话", name = "phone")
    private String phone;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String endDate;

    @ApiModelProperty(value = "状态 0：审核中 1：审核通过 2：拒绝", name = "status")
    private Integer status;

    @ApiModelProperty(value = "用户名称/联系电话", name = "userNameAndPhone")
    private String userNameAndPhone;

    @ApiModelProperty(value = "店铺名称/联系电话", name = "mchNameAndPhone")
    private String mchNameAndPhone;

    @ApiModelProperty(value = "提现类型 1银行卡  2微信余额", name = "withdrawStatus")
    private Integer withdrawStatus;

    @ApiModelProperty(value = "提现到微信余额提现状态：1进行中 2已完成 3提现失败", name = "wxStatus")
    private Integer wxStatus;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getWid()
    {
        return wid;
    }

    public void setWid(Integer wid)
    {
        this.wid = wid;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public String getUserNameAndPhone()
    {
        return userNameAndPhone;
    }

    public void setUserNameAndPhone(String userNameAndPhone)
    {
        this.userNameAndPhone = userNameAndPhone;
    }

    public String getMchNameAndPhone()
    {
        return mchNameAndPhone;
    }

    public void setMchNameAndPhone(String mchNameAndPhone)
    {
        this.mchNameAndPhone = mchNameAndPhone;
    }

    public Integer getWithdrawStatus()
    {
        return withdrawStatus;
    }

    public void setWithdrawStatus(Integer withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public Integer getWxStatus()
    {
        return wxStatus;
    }

    public void setWxStatus(Integer wxStatus)
    {
        this.wxStatus = wxStatus;
    }
}
