package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 订单列表参数
 * 【php DeliveryHelper.a_mch_order_index】
 *
 * @author Trick
 * @date 2020/11/24 17:07
 */
@ApiModel("订单列表参数")
public class MchOrderIndexVo extends MainVo
{

    @ApiModelProperty(value = "店铺id", name = "shopId")
    @ParamsMapping("shop_id")
    private int     shopId;
    @ApiModelProperty(value = "查询类型 默认全部 return=退款,payment=未付款,send=待发货, Receive=待收货", name = "orderType")
    @ParamsMapping("order_type")
    private String  orderType;
    @ApiModelProperty(value = "订单类型", name = "orderHeadrType")
    @ParamsMapping("order_headr_type")
    private String  orderHeadrType;
    @ApiModelProperty(value = "订单状态", name = "orderStauts")
    private String  orderStauts;
    @ApiModelProperty(value = "订单号", name = "keyword")
    @ParamsMapping("keyWord")
    private String  keyword;
    @ApiModelProperty(value = "平台活动id", name = "platformActivitiesId")
    @ParamsMapping("platform_activities_id")
    private Integer platformActivitiesId;

    @ApiModelProperty(value = "用户id", name = "user_id", hidden = true)
    private String userId;
    @ApiModelProperty(value = "是否展示供应商商品(不为空展示 空则反之)", name = "user_id")
    private String isSupplierPro;
    @ApiModelProperty(value = "是否展示供应商商品", name = "isSupplier")
    private String isSupplier;

    public String getIsSupplier()
    {
        return isSupplier;
    }

    public void setIsSupplier(String isSupplier)
    {
        this.isSupplier = isSupplier;
    }

    public String getIsSupplierPro()
    {
        return isSupplierPro;
    }

    public void setIsSupplierPro(String isSupplierPro)
    {
        this.isSupplierPro = isSupplierPro;
    }

    public String getOrderStauts()
    {
        return orderStauts;
    }

    public void setOrderStauts(String orderStauts)
    {
        this.orderStauts = orderStauts;
    }

    public Integer getPlatformActivitiesId()
    {
        return platformActivitiesId;
    }

    public void setPlatformActivitiesId(Integer platformActivitiesId)
    {
        this.platformActivitiesId = platformActivitiesId;
    }

    public String getOrderHeadrType()
    {
        return orderHeadrType;
    }

    public void setOrderHeadrType(String orderHeadrType)
    {
        this.orderHeadrType = orderHeadrType;
    }

    public int getShopId()
    {
        return shopId;
    }

    public void setShopId(int shopId)
    {
        this.shopId = shopId;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

}
