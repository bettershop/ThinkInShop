package com.laiketui.domain.vo.order;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;

/**
 * 评论明细查询条件
 *
 * @author Trick
 * @date 2023/3/10 20:30
 */
@ApiModel(description = "售后查询参数")
public class CommentsDetailInfoVo extends MainVo
{

    @ParamsMapping("评论id")
    private Integer cid;
    @ParamsMapping("用户id/用户名称")
    private String  key;
    @ParamsMapping("开始时间")
    private String  startDate;
    @ParamsMapping("结束时间")
    private String  endDate;

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
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
