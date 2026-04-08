package com.laiketui.domain.vo.query;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取评论明细查询参数
 *
 * @author Trick
 * @date 2023/3/7 14:43
 */
@ApiModel(description = "获取评论明细查询参数")
public class GetCommentsDetailInfoVo extends MainVo
{

    @ApiModelProperty(name = "cid", value = "评论id")
    private int    cid;
    @ApiModelProperty(name = "key", value = "用户id/用户名称")
    private String key;
    @ApiModelProperty(name = "startDate", value = "开始时间")
    private String startDate;
    @ApiModelProperty(name = "endDate", value = "结束时间")
    private String endDate;

    public int getCid()
    {
        return cid;
    }

    public void setCid(int cid)
    {
        this.cid = cid;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
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
