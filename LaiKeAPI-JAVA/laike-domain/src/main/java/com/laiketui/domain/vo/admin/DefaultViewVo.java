package com.laiketui.domain.vo.admin;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取列表数据
 *
 * @author Trick
 * @date 2020/12/28 12:03
 */
@ApiModel(description = "获取列表数据")
public class DefaultViewVo extends MainVo
{
    @ApiModelProperty(value = "分类id", name = "cid")
    private Integer cid;

    @ApiModelProperty(value = "品牌id", name = "brandId")
    private Integer brandId;

    @ApiModelProperty(value = "上下架(特殊条件7:已结束)", name = "status")
    private Integer status;

    @ApiModelProperty(value = "商品类型", name = "active")
    private Integer active;

    @ApiModelProperty(value = "标题", name = "productTitle")
    private String productTitle;

    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String mchName;

    @ApiModelProperty(value = "国家", name = "mchName")
    private int country_num;

    @ApiModelProperty(value = "销量排序 asc-升序 desc-降序", name = "IsItDescendingOrder")
    private String IsItDescendingOrder;

    public String getIsItDescendingOrder()
    {
        return IsItDescendingOrder;
    }

    public void setIsItDescendingOrder(String isItDescendingOrder)
    {
        IsItDescendingOrder = isItDescendingOrder;
    }

    @ApiModelProperty(value = "显示位置", name = "showAdr")
    private Integer showAdr;

    @ApiModelProperty(value = "商品类型 -2.当前店铺自选商品 1.实物商品 2.虚拟商品 3.自选商品 4.待审核 5.审核失败", name = "commodityType")
    private Integer commodityType = 0;

    @ApiModelProperty(value = "商品标签", name = "goodsTga")
    private Integer goodsTga;

    @ApiModelProperty(value = "预售类型 1.定金模式  2.订货模式", name = "sellType")
    private Integer sellType;

    @ApiModelProperty(value = "商品id", name = "productId")
    private Integer productId;

    @ApiModelProperty(value = "多条件", name = "condition")
    private String condition;


    public Integer getSellType()
    {
        return sellType;
    }

    public void setSellType(Integer sellType)
    {
        this.sellType = sellType;
    }

    public Integer getProductId()
    {
        return productId;
    }

    public void setProductId(Integer productId)
    {
        this.productId = productId;
    }

    public Integer getGoodsTga()
    {
        return goodsTga;
    }

    public void setGoodsTga(Integer goodsTga)
    {
        this.goodsTga = goodsTga;
    }

    public Integer getCommodityType()
    {
        return commodityType;
    }

    public void setCommodityType(Integer commodityType)
    {
        this.commodityType = commodityType;
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

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getActive()
    {
        return active;
    }

    public void setActive(Integer active)
    {
        this.active = active;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public Integer getShowAdr()
    {
        return showAdr;
    }

    public void setShowAdr(Integer showAdr)
    {
        this.showAdr = showAdr;
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
