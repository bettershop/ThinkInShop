package com.laiketui.domain.vo.order;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigDecimal;

/**
 * 下单商品信息集
 * "store_id" -> {Integer@15107} 1
 * "img" -> "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/7/20211231/1476755744083353600.png"
 * "freight" -> "199"
 * "num" -> {Integer@15107} 1
 * "weight" -> ""
 * "yprice" -> {BigDecimal@15116} "8.00"
 * "pid" -> {Integer@15118} 1890
 * "mch_id" -> {Integer@15120} 154
 * "product_title" -> "皮卡丘测试"
 * "brand_id" -> {Integer@15124} 182
 * "volume" -> {Integer@15126} 39
 * "product_class" -> "-508-524-"
 * "unit" -> "双"
 * "size" -> "尺码:3XL"
 * "price" -> {BigDecimal@15134} "888.00"
 * "is_distribution" -> {Integer@15136} 0
 * "cid" -> "17773"
 * <p>
 * "store_id" -> {Integer@15107} 1
 * "img" -> "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/7/20211231/1476755744083353600.png"
 * "freight" -> "199"
 * "num" -> {Integer@15107} 1
 * "weight" -> ""
 * "discount" -> {Integer@15136} 0
 * "yprice" -> {BigDecimal@15573} "8.00"
 * "pid" -> {Integer@15574} 1890
 * "mch_id" -> {Integer@15576} 154
 * "membership_price" -> {BigDecimal@15578} "888.000"
 * "product_title" -> "皮卡丘测试"
 * "amount_after_discount" -> {BigDecimal@15582} "878.000"
 * "brand_id" -> {Integer@15584} 182
 * "volume" -> {Integer@15126} 39
 * "product_class" -> "-508-524-"
 * "unit" -> "双"
 * "size" -> "尺码:3XL"
 * "coupon_id" -> "0,797"
 * "price" -> {BigDecimal@15594} "888.00"
 * "freight_price" -> {BigDecimal@15595} "12"
 * "is_distribution" -> {Integer@15136} 0
 * "cid" -> "17773"
 *
 * @author Trick
 * @date 2022/2/10 10:58
 */
public class OrderProductsVo
{
    /**
     * 商城id
     */
    @JSONField(name = "store_id")
    private Integer storeId;


    /**
     * 规格图片
     */
    private String img;


    /**
     * 运费id
     */
    @JSONField(name = "freight")
    private String freightId;

    /**
     * 商品数量
     */
    private BigDecimal num;

    /**
     * 商品单位
     */
    private String weight;

    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 商品原价
     */
    private BigDecimal yprice;

    /**
     * 店铺id
     */
    @JSONField(name = "mch_id")
    private Integer mchId;

    /**
     * 没打折扣之前的商品价格(不算运费)
     */
    @JSONField(name = "membership_price")
    private BigDecimal membershipPrice;

    /**
     * 折扣后的价格
     */
    @JSONField(name = "amount_after_discount")
    private BigDecimal amountAfterDiscount;


    /**
     * 商品名称
     */
    @JSONField(name = "product_title")
    private String productTitle;

    /**
     * 品牌id
     */
    @JSONField(name = "brand_id")
    private Integer branId;

    /**
     * 销量
     */
    private Integer volume;

    /**
     * 商品类别
     */
    @JSONField(name = "product_class")
    private String productClass;

    /**
     * 单位
     */
    @JSONField(name = "unit")
    private String unit;

    /**
     * 商品规格
     */
    @JSONField(name = "size")
    private String size;

    /**
     * 结算时用的优惠券  0,123
     */
    @JSONField(name = "coupon_id")
    private String couponIds;

    /**
     * 商品价格
     */
    @JSONField(name = "price")
    private BigDecimal price;

    /**
     * 当前商品运费
     */
    @JSONField(name = "freight_price")
    private BigDecimal freightPrice;

    /**
     * 是否是分销商品
     */
    @JSONField(name = "is_distribution")
    private Integer isDistribution;

    /**
     * 单位
     */
    @JSONField(name = "规格id")
    private Integer cid;

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public String getImg()
    {
        return img;
    }

    public void setImg(String img)
    {
        this.img = img;
    }

    public String getFreightId()
    {
        return freightId;
    }

    public void setFreightId(String freightId)
    {
        this.freightId = freightId;
    }

    public BigDecimal getNum()
    {
        return num;
    }

    public void setNum(BigDecimal num)
    {
        this.num = num;
    }

    public String getWeight()
    {
        return weight;
    }

    public void setWeight(String weight)
    {
        this.weight = weight;
    }

    public BigDecimal getDiscount()
    {
        return discount;
    }

    public void setDiscount(BigDecimal discount)
    {
        this.discount = discount;
    }

    public BigDecimal getYprice()
    {
        return yprice;
    }

    public void setYprice(BigDecimal yprice)
    {
        this.yprice = yprice;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public BigDecimal getMembershipPrice()
    {
        return membershipPrice;
    }

    public void setMembershipPrice(BigDecimal membershipPrice)
    {
        this.membershipPrice = membershipPrice;
    }

    public BigDecimal getAmountAfterDiscount()
    {
        return amountAfterDiscount;
    }

    public void setAmountAfterDiscount(BigDecimal amountAfterDiscount)
    {
        this.amountAfterDiscount = amountAfterDiscount;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public Integer getBranId()
    {
        return branId;
    }

    public void setBranId(Integer branId)
    {
        this.branId = branId;
    }

    public Integer getVolume()
    {
        return volume;
    }

    public void setVolume(Integer volume)
    {
        this.volume = volume;
    }

    public String getProductClass()
    {
        return productClass;
    }

    public void setProductClass(String productClass)
    {
        this.productClass = productClass;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getSize()
    {
        return size;
    }

    public void setSize(String size)
    {
        this.size = size;
    }

    public String getCouponIds()
    {
        return couponIds;
    }

    public void setCouponIds(String couponIds)
    {
        this.couponIds = couponIds;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getFreightPrice()
    {
        return freightPrice;
    }

    public void setFreightPrice(BigDecimal freightPrice)
    {
        this.freightPrice = freightPrice;
    }

    public Integer getIsDistribution()
    {
        return isDistribution;
    }

    public void setIsDistribution(Integer isDistribution)
    {
        this.isDistribution = isDistribution;
    }

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }
}
