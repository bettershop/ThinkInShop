package com.laiketui.domain.vo.auction;


import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加场次参数
 *
 * @author Trick
 * @date 2022/7/1 15:30
 */
@ApiModel(description = "添加/编辑 场次参数")
public class AddSessionVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "竞拍场次id")
    private String id;
    @ApiModelProperty(name = "specialId", value = "竞拍专场id")
    private String specialId;
    @ApiModelProperty(name = "title", value = "场次名称")
    private String title;
    @ApiModelProperty(name = "startDate", value = "开始时间")
    private String startDate;
    @ApiModelProperty(name = "endDate", value = "结束时间")
    private String endDate;

    @ApiModelProperty(name = "attrIds", value = "场次商品规格id集[{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]")
    private String attrIds;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSpecialId()
    {
        return specialId;
    }

    public void setSpecialId(String specialId)
    {
        this.specialId = specialId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public String getAttrIds()
    {
        return attrIds;
    }

    public void setAttrIds(String attrIds)
    {
        this.attrIds = attrIds;
    }
}
