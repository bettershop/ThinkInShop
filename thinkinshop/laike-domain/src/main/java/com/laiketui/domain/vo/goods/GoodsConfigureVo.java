package com.laiketui.domain.vo.goods;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取商品(规格)列表 参数
 *
 * @author Trick
 * @date 2021/8/3 16:58
 */
public class GoodsConfigureVo extends MainVo
{
    @ApiModelProperty(value = "分类id", name = "cid")
    private Integer cid;
    @ApiModelProperty(value = "品牌id", name = "brandId")
    private Integer brandId;
    @ApiModelProperty(value = "商品标题", name = "productTitle")
    private String  productTitle;
    @ApiModelProperty(value = "楼层id", name = "blockId")
    private String  blockId;
    @ApiModelProperty(value = "是否有供应商商品 1有 2不包含", name = "idSupplier")
    private Integer isSupplier;

    @ApiModelProperty(value = "是否自营 1：是")
    private Integer isZy;

    public Integer getIsZy()
    {
        return isZy;
    }

    public void setIsZy(Integer isZy)
    {
        this.isZy = isZy;
    }

    public String getBlockId()
    {
        return blockId;
    }

    public void setBlockId(String blockId)
    {
        this.blockId = blockId;
    }

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

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public Integer getIsSupplier()
    {
        return isSupplier;
    }

    public void setIsSupplier(Integer isSupplier)
    {
        this.isSupplier = isSupplier;
    }
}
