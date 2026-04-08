package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取用户资金列表参数
 *
 * @author Trick
 * @date 2021/1/11 15:51
 */
@ApiModel(description = "获取用户资金列表参数")
public class UserMoneyVo extends MainVo
{

    @ApiModelProperty(value = "用户id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "userid", name = "userid")
    private String  userid;

    @ApiModelProperty(value = "会员名称", name = "userName")
    private String userName;
    @ApiModelProperty(value = "用户来源", name = "source")
    private int    source;
    @ApiModelProperty(value = "开始时间", name = "startDate")
    private String startDate;
    @ApiModelProperty(value = "结束时间", name = "endDate")
    private String endDate;
    @ApiModelProperty(value = "联系电话", name = "tel")
    private String tel;
    @ApiModelProperty(value = "类型", name = "oType")
    private String oType;
    @ApiModelProperty(value = "入账支出", name = "ostatus")
    private String ostatus;
    @ApiModelProperty(value = "是否分页", name = "isPage")
    private String isPage;


    public String getIsPage()
    {
        return isPage;
    }

    public void setIsPage(String isPage)
    {
        this.isPage = isPage;
    }

    public String getostatus()
    {
        return ostatus;
    }

    public void setostatus(String ostatus)
    {
        this.ostatus = ostatus;
    }

    public String getUserid()
    {
        return userid;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }


    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public int getSource()
    {
        return source;
    }

    public void setSource(int source)
    {
        this.source = source;
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

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getotype()
    {
        return oType;
    }

    public void setoType(String oType)
    {
        this.oType = oType;
    }
}
