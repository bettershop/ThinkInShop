package com.laiketui.domain.vo.plugin.member;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取会员商品列表数据
 *
 * @author sunH_
 * @date 2022/07/01 12:03
 */
@ApiModel(description = "获取会员商品列表数据")
public class MemberProQueryVo extends MainVo
{
    @ApiModelProperty(value = "分类id", name = "cid")
    private Integer cid;

    @ApiModelProperty(value = "品牌id", name = "brandId")
    private Integer brandId;

    @ApiModelProperty(value = "上下架 1:待上架 2:上架 3:下架", name = "status")
    private Integer status;

    @ApiModelProperty(value = "标题", name = "productTitle")
    private String productTitle;

    @ApiModelProperty(value = "商品id", name = "productId")
    private Integer productId;

    @ApiModelProperty(value = "商品编号/名称", name = "condition")
    private String condition;

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public Integer getBrandId()
    {
        return brandId;
    }

    public void setBrandId(Integer brandId)
    {
        this.brandId = brandId;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public Integer getProductId()
    {
        return productId;
    }

    public void setProductId(Integer productId)
    {
        this.productId = productId;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }
}
