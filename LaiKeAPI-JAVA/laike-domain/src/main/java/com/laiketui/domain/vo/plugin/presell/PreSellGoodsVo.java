package com.laiketui.domain.vo.plugin.presell;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑秒杀配置信息
 *
 * @author sunH_
 * @date 2021/12/28 16:51
 */
@ApiModel(description = "预售配置信息")
public class PreSellGoodsVo extends MainVo
{
    @ApiModelProperty(value = "获取类型 1.商城 2.店铺")
    private Integer type;
    @ApiModelProperty(value = "店铺id")
    private Integer mchId;
    @ApiModelProperty(value = "商品名称")
    private String  title;

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
