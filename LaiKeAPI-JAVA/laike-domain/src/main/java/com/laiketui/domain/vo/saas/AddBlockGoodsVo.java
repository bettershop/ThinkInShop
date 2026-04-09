package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 添加楼层商品
 *
 * @author Trick
 * @date 2023/2/15 11:05
 */
@ApiModel(description = "添加楼层商品")
public class AddBlockGoodsVo extends MainVo implements Serializable
{

    @ApiModelProperty(value = "楼层id", name = "id")
    private String id;
    @ApiModelProperty(value = "商品id集", name = "goodsId")
    private String goodsId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(String goodsId)
    {
        this.goodsId = goodsId;
    }
}
