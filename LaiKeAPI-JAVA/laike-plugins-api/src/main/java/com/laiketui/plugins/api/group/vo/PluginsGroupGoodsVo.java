package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取移动端商品列表 查询参数
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@ApiModel(description = "获取移动端商品列表 查询参数")
public class PluginsGroupGoodsVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private Integer id;
    @ApiModelProperty(value = "店铺id", name = "mchId")
    private Integer mchId;

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
}
