package com.laiketui.domain.vo.order;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigDecimal;
import java.util.List;

/**
 * 下单店铺商品信息集
 * <p>
 * "grade_rate_amount" -> {Double@15518} 0.0
 * "shop_id" -> {Integer@15519} 154
 * "products_num" -> {BigDecimal@15521} "1"
 * "shop_subtotal" -> {BigDecimal@15523} "900.0"
 * "freight_price" -> {Double@15525} 12.0
 * "list" -> {ArrayList@15526}  size = 1
 * "shop_name" -> "平台自营店123"
 * "shop_logo" -> "1462987193744859136.jpg"
 * "product_total" -> {Double@15529} 888.0
 * "coupon_list" -> {ArrayList@15531}  size = 0
 *
 * @author Trick
 * @date 2022/2/10 10:58
 */
public class OrderMchProductsVo
{

    /**
     * 店铺id
     */
    @JSONField(name = "shop_id")
    private Integer shopId;

    /**
     * 商品集
     */
    @JSONField(name = "list")
    private List<OrderProductsVo> goodsInfoList;

    /**
     * 店铺名称
     */
    @JSONField(name = "shop_name")
    private String shopName;

    /**
     * 当前店铺商品总数量
     */
    @JSONField(name = "products_num")
    private String productsNum;

    /**
     * 店铺logo
     */
    @JSONField(name = "shop_logo")
    private String shopLogo;

    /**
     * 商品总价(不算运费)
     */
    @JSONField(name = "product_total")
    private BigDecimal productTotal;

    /**
     * 当前店铺商品总价(算运费)
     */
    @JSONField(name = "shop_subtotal")
    private BigDecimal shopSubtotal;

    /**
     * 当前店铺总运费
     */
    @JSONField(name = "freight_price")
    private BigDecimal freightPrice;

    /**
     * 当前店铺可用优惠券
     */
    @JSONField(name = "coupon_list")
    private List<Integer> couponList;

    public Integer getShopId()
    {
        return shopId;
    }

    public void setShopId(Integer shopId)
    {
        this.shopId = shopId;
    }

    public List<OrderProductsVo> getGoodsInfoList()
    {
        return goodsInfoList;
    }

    public void setGoodsInfoList(List<OrderProductsVo> goodsInfoList)
    {
        this.goodsInfoList = goodsInfoList;
    }

    public String getShopName()
    {
        return shopName;
    }

    public void setShopName(String shopName)
    {
        this.shopName = shopName;
    }

    public String getProductsNum()
    {
        return productsNum;
    }

    public void setProductsNum(String productsNum)
    {
        this.productsNum = productsNum;
    }

    public String getShopLogo()
    {
        return shopLogo;
    }

    public void setShopLogo(String shopLogo)
    {
        this.shopLogo = shopLogo;
    }

    public BigDecimal getProductTotal()
    {
        return productTotal;
    }

    public void setProductTotal(BigDecimal productTotal)
    {
        this.productTotal = productTotal;
    }

    public BigDecimal getShopSubtotal()
    {
        return shopSubtotal;
    }

    public void setShopSubtotal(BigDecimal shopSubtotal)
    {
        this.shopSubtotal = shopSubtotal;
    }

    public BigDecimal getFreightPrice()
    {
        return freightPrice;
    }

    public void setFreightPrice(BigDecimal freightPrice)
    {
        this.freightPrice = freightPrice;
    }

    public List<Integer> getCouponList()
    {
        return couponList;
    }

    public void setCouponList(List<Integer> couponList)
    {
        this.couponList = couponList;
    }
}
