package com.laiketui.domain.vo.coupon;


import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 店铺查看优惠卷领取信息
 *
 * @author Trick
 * @date 2023/3/7 17:44
 */
@ApiModel(description = "店铺查看优惠卷领取信息查询条件")
public class SeeCouponVo extends MainVo
{

    @ApiModelProperty(value = "活动id", name = "id")
    private Integer   id;
    @ApiModelProperty(value = "店铺id", name = "mchId")
    private Integer   mchId;
    @ApiModelProperty(value = "优惠卷状态", name = "status")
    private Integer   status;
    @ApiModelProperty(value = "优惠券类型", name = "type")
    private Integer   type;
    @ApiModelProperty(value = "订单号", name = "sNo")
    private String    sNo;
    @ApiModelProperty(value = "用户名称", name = "name")
    private String    name;
    @ApiModelProperty(value = "订单号/用户名称", name = "keyWord")
    private String    keyWord;
    @ApiModelProperty(value = "导出参数", name = "pageTo")
    private String    pageTo;
    private PageModel pageModel;
    @ApiModelProperty(value = "是否为赠送 0.不为赠送 1.赠送", name = "isFree")
    private Integer   isFree = 0;




    //是否根据userid分组
    private boolean isGroupByUserId = false;

    public boolean isGroupByUserId()
    {
        return isGroupByUserId;
    }

    public void setGroupByUserId(boolean groupByUserId)
    {
        isGroupByUserId = groupByUserId;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKeyWord()
    {
        return keyWord;
    }

    public void setKeyWord(String keyWord)
    {
        this.keyWord = keyWord;
    }

    public String getPageTo()
    {
        return pageTo;
    }

    public void setPageTo(String pageTo)
    {
        this.pageTo = pageTo;
    }

    public PageModel getPageModel()
    {
        return pageModel;
    }

    public void setPageModel(PageModel pageModel)
    {
        this.pageModel = pageModel;
    }

    public Integer getIsFree()
    {
        return isFree;
    }

    public void setIsFree(Integer isFree)
    {
        this.isFree = isFree;
    }
}
