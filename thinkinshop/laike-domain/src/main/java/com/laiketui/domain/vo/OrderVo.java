package com.laiketui.domain.vo;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.domain.user.User;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 订单处理参数处理类
 *
 * @author wangxian
 */
@Validated
public class OrderVo extends MainVo
{

    /**
     * 优惠券
     */
    public static final String COUPON = "coupon";

    /**
     * 满减
     */
    public static final String SUBTRACTION = "subtraction";

    /**
     * 未使用优惠
     */
    public static final String NO_DISCOUNT = "nodiscount";


    /**
     * 插件主键id
     */
    @ParamsMapping({"jifen_id", "bargain_id", "sec_id", "fs_id"})
    private Integer mainId;

    /**
     * 凭证
     */
    private String voucher;
    /**
     * 插件主键uuid
     */
    private String  pluginId;

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String pluginId)
    {
        this.pluginId = pluginId;
    }


    /**
     * 店铺id
     */
    private Integer mchId;

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    /**
     * 订单类型
     */
    @ParamsMapping({"product_type", "order_type"})
    private String type = "GM";

    public Integer getMainId()
    {
        return mainId;
    }

    public void setMainId(Integer mainId)
    {
        this.mainId = mainId;
    }

    /**
     * 订单号
     */
    @ParamsMapping("id")
    private String sNo;

    /**
     * 商品详情信息 [{"pid":"979"},{"cid":"5648"},{"num":1}]
     * 【拼团参数结构->ProductInfoParamVo】
     */
    @ParamsMapping("product")
    private String productsInfo;

    /**
     * 购物车记录id 逗号分割 123,234
     */
    @ParamsMapping("cart_id")
    private String carts;


    /**
     * 是否调起钱包支付
     */
    private Boolean pay = false;

    /**
     * 默认为0 再次购买=1
     */
    @ParamsMapping("buy_type")
    private Integer buyType = 0;

    /**
     * 商品类型
     */
    private String productType = "GM";

    /**
     * 积分抵扣金额
     */
    private BigDecimal scoreDeductionPrice;

    /**
     * 积分抵扣
     */
    private Integer scoreDeduction;

    /**
     * 积分比例配置
     */
    private String scoreRatio;

    /**
     * 是否为分销商品 默认0 不是分销商品；1：分销商品
     */
    @ParamsMapping("is_distribution")
    private int isDistribution = 0;

    /**
     * 上级id,一般用于推荐人
     */
    private String fatherId;

    /**
     * 是否是店铺查看订单详情
     */
    private Integer isMch;

    private String freights;

    public Boolean getPay()
    {
        return pay;
    }

    public void setPay(Boolean pay)
    {
        this.pay = pay;
    }

    public String getFreights()
    {
        return freights;
    }

    public void setFreights(String freights)
    {
        this.freights = freights;
    }

    public Integer getIsMch()
    {
        return isMch;
    }

    public void setIsMch(Integer isMch)
    {
        this.isMch = isMch;
    }
    public BigDecimal getScoreDeductionPrice()
    {
        return scoreDeductionPrice;
    }

    public void setScoreDeductionPrice(BigDecimal scoreDeductionPrice)
    {
        this.scoreDeductionPrice = scoreDeductionPrice;
    }

    public String getScoreRatio()
    {
        return scoreRatio;
    }

    public void setScoreRatio(String scoreRatio)
    {
        this.scoreRatio = scoreRatio;
    }

    public Integer getScoreDeduction()
    {
        return scoreDeduction;
    }

    public void setScoreDeduction(Integer scoreDeduction)
    {
        this.scoreDeduction = scoreDeduction;
    }

    public String getFatherId()
    {
        return fatherId;
    }

    public void setFatherId(String fatherId)
    {
        this.fatherId = fatherId;
    }

    /**
     * 不展示的商品类型
     */
    private String oTypeNotIn;

    public String getoTypeNotIn()
    {
        return oTypeNotIn;
    }

    public void setoTypeNotIn(String oTypeNotIn)
    {
        this.oTypeNotIn = oTypeNotIn;
    }

    /**
     * 收货地址id
     */
    @ParamsMapping("address_id")
    private Integer addressId = -1;

    /**
     * 门店地址id shop_address_id
     */
    @ParamsMapping("shop_address_id")
    private Integer shopAddressId = 0;

    /**
     * 是否是商品自配，此参数0为快递，1为自提，2为商家自配
     */
    @Transient
    private Integer is_self_delivery;

    /**
     * 惠惠券id等信息 0，0，flag,优惠类型 不能同时使用优惠券和满减
     * 0,0,0,no_discount : 表示 有两个点的没有使用优惠券，并且平台优惠券和满减也没有使用
     * 0,685,coupon 表示没有使用店铺优惠券，使用了平台优惠券
     */
    @ParamsMapping("coupon_id")
    private String couponId;


    /**
     * 规格id
     */
    @ParamsMapping("attrId")
    private Integer attrId;

    /**
     * 订单号【编辑订单】
     */
    private String orderNo;

    /**
     * 支付订单号
     */
    private String realSno;

    /**
     * 购买人id
     */
    private Integer uid;


    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    /**
     * 是否选择了优惠券：
     * false   未选择 将自动计算最优优惠卷
     * true    已选择 只计算当前优惠额度
     */
    /*@ParamsMapping("hasChoiseCoupon")*/
    private boolean canshu = true;

    /**
     * 会员特惠 兑换券级别
     */
    private Integer gradeLevel = 0;

    /**
     * 支付时使用的优惠券id
     */
    private String  useCouponId;
    /**
     * 用户使用积分
     */
    private Integer allow = 0;

    /**
     * 订单备注
     */
    private String remarks;


    /**
     * 跳转类型 1 从订单详情跳
     */
    private Integer skipType;

    /**
     * 支付类型()
     */
    @ParamsMapping("pay_type")
    private String payType;

    /**
     * { sNo: me.sNo, total: me.z_price_, order_id: me.orde_id }
     */
    @ParamsMapping("order_list")
    private String orderList;

    /**
     * 开团
     */
    private String tzkt;

    public String getTzkt()
    {
        return tzkt;
    }

    public void setTzkt(String tzkt)
    {
        this.tzkt = tzkt;
    }

    /**
     * 订单列表搜索
     */
    @ApiModelProperty(value = "关键字", name = "keyword")
    @ParamsMapping("keyword")
    private String keyword;

    @ApiModelProperty(value = "限时秒杀加购商品id", name = "add_good")
    private String add_good;

    @ApiModelProperty(value = "直播间id", name = "roomId")
    private String roomId;

    public String getRoomId()
    {
        return roomId;
    }

    public void setRoomId(String roomId)
    {
        this.roomId = roomId;
    }

    public Integer getIs_self_delivery()
    {
        return is_self_delivery;
    }

    public void setIs_self_delivery(Integer is_self_delivery)
    {
        this.is_self_delivery = is_self_delivery;
    }

    public String getAdd_good()
    {
        return add_good;
    }

    public void setAdd_good(String add_good)
    {
        this.add_good = add_good;
    }

    private User user;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Integer getSkipType()
    {
        return skipType;
    }

    public void setSkipType(Integer skipType)
    {
        this.skipType = skipType;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getRealSno()
    {
        return realSno;
    }

    public void setRealSno(String realSno)
    {
        this.realSno = realSno;
    }

    /**
     * 商品名称/订单号
     */
    private String ordervalue;

    /**
     * 订单查询类型 前端orderType需要映射到此字段
     */
    @ApiModelProperty(value = "订单查询类型 payment(待付款) send(代发货) receipt(待收货) evaluete(待评价) return(售后) write(待核销)", name = "queryOrderType")
    @ParamsMapping("queryOrderType")
    private String queryOrderType;

    public String getQueryOrderType()
    {
        return queryOrderType;
    }

    public void setQueryOrderType(String queryOrderType)
    {
        this.queryOrderType = queryOrderType;
    }

    /**
     * 此字段和type一致
     */
    private String orderType;

    /**
     * 订单失效天数 默认2天
     */
    private int orderFailure = 2;

    /**
     * 单位
     */
    private String company = "hour";

    /**
     * 订单id
     */
    @ParamsMapping("order_id")
    private int orderId;

    /**
     * 购物车id
     */
    @ParamsMapping("cart_id")
    private String cartId;

    /**
     * 链接
     */
    private String url;

    /**
     * 支付方式
     */
    private String payClassName;

    public boolean getIsPlugin()
    {
        return isPlugin;
    }

    public void setIsPlugin(boolean isPlugin)
    {
        this.isPlugin = isPlugin;
    }

    /**
     * 售后类型
     */
    private Integer rtype;

    /**
     * 订单详情id 1,12,3
     */
    @ParamsMapping("order_details_id")
    private String orderDetailsId;

    /**
     * 查看物流接口
     */
    @ParamsMapping("o_source")
    private String oSource;

    /**
     * 支付目标
     */
    @ApiModelProperty(value = "支付目标 1.定金  2.尾款  3.全款", name = "payTarget")
    private Integer payTarget;

    /**
     * 远程调用携带
     */
    private String userId;

    /**
     * 会员价格处理 0.普通 1.会员
     */
    private Integer vipSource = 0;

    /**
     * 多条件
     */
    private String condition;

    /**
     * 自提人姓名
     */
    private String fullName;

    /**
     * 自提人电话号
     */
    private String fullPhone;

    /**
     * 是否是插件主动下单
     */
    private boolean isPlugin;

    /**
     * 核销时间段id
     */
    private Integer mchStoreWrite;

    /**
     * 核销时间
     */
    private String mchStoreWriteTime;

    /**
     * 商家自配选择的自配时间
     */
    private String deliveryTime;

    /**
     * 商家自配选择的自配时间段 1为上午，2为下午
     */
    private String deliveryPeriod;

    /**
     * 货币编码：支付和退款用
     */
    private String currency_code;

    /**
     * 货币符号
     */
    private String currency_symbol;

    /**
     * 汇率：支付和退款用、界面展示计算用
     */
    private BigDecimal exchange_rate;

    public String getCurrency_code()
    {
        return currency_code;
    }

    public void setCurrency_code(String currency_code)
    {
        this.currency_code = currency_code;
    }

    public String getCurrency_symbol()
    {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol)
    {
        this.currency_symbol = currency_symbol;
    }

    public BigDecimal getExchange_rate()
    {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate)
    {
        this.exchange_rate = exchange_rate;
    }

    public String getDeliveryPeriod()
    {
        return deliveryPeriod;
    }

    public void setDeliveryPeriod(String deliveryPeriod)
    {
        this.deliveryPeriod = deliveryPeriod;
    }

    public String getDeliveryTime()
    {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime)
    {
        this.deliveryTime = deliveryTime;
    }

    public Integer getUid()
    {
        return uid;
    }

    public void setUid(Integer uid)
    {
        this.uid = uid;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullPhone()
    {
        return fullPhone;
    }

    public void setFullPhone(String fullPhone)
    {
        this.fullPhone = fullPhone;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    /**
     * 订单类型集合
     */
    private List<String> orderTypeList = Collections.singletonList(DictionaryConst.OrdersType.ORDERS_HEADER_GM);

    public List<String> getOrderTypeList()
    {
        return orderTypeList;
    }

    public void setOrderTypeList(List<String> orderTypeList)
    {
        this.orderTypeList = orderTypeList;
    }

    public Integer getPayTarget()
    {
        return payTarget;
    }

    public void setPayTarget(Integer payTarget)
    {
        this.payTarget = payTarget;
    }

    public String getoSource()
    {
        return oSource;
    }

    public void setoSource(String oSource)
    {
        this.oSource = oSource;
    }

    public String getOrderDetailsId()
    {
        return orderDetailsId;
    }

    public void setOrderDetailsId(String orderDetailsId)
    {
        this.orderDetailsId = orderDetailsId;
    }

    public Integer getRtype()
    {
        return rtype;
    }

    public void setRtype(Integer rtype)
    {
        this.rtype = rtype;
    }

    public String getPayClassName()
    {
        return payClassName;
    }

    public void setPayClassName(String payClassName)
    {
        this.payClassName = payClassName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getCartId()
    {
        return cartId;
    }

    public void setCartId(String cartId)
    {
        this.cartId = cartId;
    }

    public int getOrderId()
    {
        return orderId;
    }

    public void setOrderId(int orderId)
    {
        this.orderId = orderId;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public int getOrderFailure()
    {
        return orderFailure;
    }

    public void setOrderFailure(int orderFailure)
    {
        this.orderFailure = orderFailure;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = getType();
    }

    public String getOrdervalue()
    {
        return ordervalue;
    }

    public void setOrdervalue(String ordervalue)
    {
        this.ordervalue = ordervalue;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getOrderList()
    {
        return orderList;
    }

    public void setOrderList(String orderList)
    {
        this.orderList = orderList;
    }

    public String getPayType()
    {
        return payType;
    }

    public void setPayType(String payType)
    {
        this.payType = payType;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public Integer getAllow()
    {
        return allow;
    }

    public void setAllow(Integer allow)
    {
        this.allow = allow;
    }

    public String getUseCouponId()
    {
        return useCouponId;
    }

    public void setUseCouponId(String useCouponId)
    {
        this.useCouponId = useCouponId;
    }

    public boolean getCanshu()
    {
        return canshu;
    }

    public Integer getGradeLevel()
    {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel)
    {
        this.gradeLevel = gradeLevel;
    }

    public String getCouponId()
    {
        return couponId;
    }

    public void setCouponId(String couponId)
    {
        this.couponId = couponId;
    }

    public Integer getShopAddressId()
    {
        return shopAddressId;
    }

    public void setShopAddressId(Integer shopAddressId)
    {
        this.shopAddressId = shopAddressId;
    }

    public Integer getAddressId()
    {
        return addressId;
    }

    public void setAddressId(Integer addressId)
    {
        this.addressId = addressId;
    }

    public int getIsDistribution()
    {
        return isDistribution;
    }

    public void setIsDistribution(int isDistribution)
    {
        this.isDistribution = isDistribution;
    }

    public String getProductType()
    {
        return productType;
    }

    public void setProductType(String productType)
    {
        this.productType = productType;
    }

    public Integer getBuyType()
    {
        return buyType;
    }

    public void setBuyType(Integer buyType)
    {
        this.buyType = buyType;
    }

    public String getCarts()
    {
        return carts;
    }

    public void setCarts(String carts)
    {
        this.carts = carts;
    }

    public String getProductsInfo()
    {
        return productsInfo;
    }

    public void setProductsInfo(String productsInfo)
    {
        this.productsInfo = productsInfo;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public void setCanshu(boolean canshu)
    {
        this.canshu = canshu;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
        this.productType = type;
        this.orderType = type;
    }


    public Integer getVipSource()
    {
        return vipSource;
    }

    public void setVipSource(Integer vipSource)
    {
        this.vipSource = vipSource;
    }

    public Integer getMchStoreWrite()
    {
        return mchStoreWrite;
    }

    public void setMchStoreWrite(Integer mchStoreWrite)
    {
        this.mchStoreWrite = mchStoreWrite;
    }

    public String getMchStoreWriteTime()
    {
        return mchStoreWriteTime;
    }

    public void setMchStoreWriteTime(String mchStoreWriteTime)
    {
        this.mchStoreWriteTime = mchStoreWriteTime;
    }

    @Override
    public String toString()
    {
        return "OrderVo{" +
                "mainId=" + mainId +
                ", pluginId='" + pluginId + '\'' +
                ", mchId=" + mchId +
                ", type='" + type + '\'' +
                ", sNo='" + sNo + '\'' +
                ", productsInfo='" + productsInfo + '\'' +
                ", carts='" + carts + '\'' +
                ", pay=" + pay +
                ", buyType=" + buyType +
                ", productType='" + productType + '\'' +
                ", scoreDeductionPrice=" + scoreDeductionPrice +
                ", scoreDeduction=" + scoreDeduction +
                ", scoreRatio='" + scoreRatio + '\'' +
                ", isDistribution=" + isDistribution +
                ", fatherId='" + fatherId + '\'' +
                ", isMch=" + isMch +
                ", freights='" + freights + '\'' +
                ", oTypeNotIn='" + oTypeNotIn + '\'' +
                ", addressId=" + addressId +
                ", shopAddressId=" + shopAddressId +
                ", is_self_delivery=" + is_self_delivery +
                ", couponId='" + couponId + '\'' +
                ", attrId=" + attrId +
                ", orderNo='" + orderNo + '\'' +
                ", realSno='" + realSno + '\'' +
                ", uid=" + uid +
                ", canshu=" + canshu +
                ", gradeLevel=" + gradeLevel +
                ", useCouponId='" + useCouponId + '\'' +
                ", allow=" + allow +
                ", remarks='" + remarks + '\'' +
                ", skipType=" + skipType +
                ", payType='" + payType + '\'' +
                ", orderList='" + orderList + '\'' +
                ", tzkt='" + tzkt + '\'' +
                ", keyword='" + keyword + '\'' +
                ", add_good='" + add_good + '\'' +
                ", roomId='" + roomId + '\'' +
                ", user=" + user +
                ", ordervalue='" + ordervalue + '\'' +
                ", queryOrderType='" + queryOrderType + '\'' +
                ", orderType='" + orderType + '\'' +
                ", orderFailure=" + orderFailure +
                ", company='" + company + '\'' +
                ", orderId=" + orderId +
                ", cartId='" + cartId + '\'' +
                ", url='" + url + '\'' +
                ", payClassName='" + payClassName + '\'' +
                ", rtype=" + rtype +
                ", orderDetailsId='" + orderDetailsId + '\'' +
                ", oSource='" + oSource + '\'' +
                ", payTarget=" + payTarget +
                ", userId='" + userId + '\'' +
                ", vipSource=" + vipSource +
                ", condition='" + condition + '\'' +
                ", fullName='" + fullName + '\'' +
                ", fullPhone='" + fullPhone + '\'' +
                ", isPlugin=" + isPlugin +
                ", mchStoreWrite=" + mchStoreWrite +
                ", mchStoreWriteTime='" + mchStoreWriteTime + '\'' +
                ", deliveryTime='" + deliveryTime + '\'' +
                ", deliveryPeriod='" + deliveryPeriod + '\'' +
                ", currency_code='" + currency_code + '\'' +
                ", currency_symbol='" + currency_symbol + '\'' +
                ", exchange_rate=" + exchange_rate +
                ", orderTypeList=" + orderTypeList +
                '}';
    }
}
