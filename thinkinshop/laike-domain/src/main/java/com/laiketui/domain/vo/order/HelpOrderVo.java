package com.laiketui.domain.vo.order;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 代客下单
 *
 * @author Trick
 * @date 2021/8/2 17:50
 */
@ApiModel(description = "代客下单")
public class HelpOrderVo extends MainVo
{

    @ApiModelProperty(value = "会员userId", name = "userId", required = true)
    private String  userId;
    @ApiModelProperty(value = "商品信息json数组 [{\"id\":\"17028\",\"pid\":\"1475\",\"num\":1, freight:10}...] freight == -1 须计算运费", name = "products", required = true)
    private String  products;
    @ApiModelProperty(value = "优惠金额", name = "wipeOff", required = true)
    private String  wipeOff;
    @ApiModelProperty(value = "会员收货地址id", name = "addressId", required = true)
    private Integer addressId;
    @ApiModelProperty(value = "线下支付0 余额支付1", name = "isOfflinePayment", required = true)
    private Integer isOfflinePayment;


    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getProducts()
    {
        return products;
    }

    public void setProducts(String products)
    {
        this.products = products;
    }

    public String getWipeOff()
    {
        return wipeOff;
    }

    public void setWipeOff(String wipeOff)
    {
        this.wipeOff = wipeOff;
    }

    public Integer getAddressId()
    {
        return addressId;
    }

    public void setAddressId(Integer addressId)
    {
        this.addressId = addressId;
    }

    public Integer getIsOfflinePayment()
    {
        return isOfflinePayment;
    }

    public void setIsOfflinePayment(Integer isOfflinePayment)
    {
        this.isOfflinePayment = isOfflinePayment;
    }
}
