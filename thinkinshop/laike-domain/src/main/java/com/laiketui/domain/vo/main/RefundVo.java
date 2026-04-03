package com.laiketui.domain.vo.main;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 售后参数
 *
 * @author Trick
 * @date 2020/12/2 11:46
 */
@ApiModel(description = "售后参数")
@Setter
@Getter
public class RefundVo extends MainVo
{

    @ApiModelProperty(value = "用户名称", hidden = true)
    private String adminName;
    @ApiModelProperty(value = "售后id", name = "id")
    private int id;
    @ApiModelProperty(value = "售后类型  1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束", name = "type")
    @ParamsMapping("r_type")
    private Integer type;
    @ApiModelProperty(value = "拒绝理由", name = "text")
    private String text;
    @ApiModelProperty(value = "退款金额", name = "price")
    private BigDecimal price = new BigDecimal("0");
    @ApiModelProperty(value = "店铺id", name = "mchId")
    @ParamsMapping("shop_id")
    private int mchId;


    @ApiModelProperty(value = "快递公司编号(商品寄回流程)", name = "expressId")
    @ParamsMapping("express_id")
    private Integer expressId;
    @ApiModelProperty(value = "快递单号(商品寄回流程)", name = "courierNum")
    @ParamsMapping("courier_num")
    private String courierNum;
    @ApiModelProperty(value = "订单号", name = "sNo")
    private String sNo;

    @ApiModelProperty(value = "操作者(用于区分供应商端操作与其他端操作)", name = "operator")
    private String operator;

    @ApiModelProperty(value = "订单详情id", name = "orderDetailId")
    private Integer orderDetailId;

    @ApiModelProperty(value = "售后类型", name = "orderDetailId")
    private Integer reType;

    /**
     * 售后逻辑都统一,如果插件有特殊流程则走回调
     * add by trick 2023-04-20 12:06:49
     */
    @ApiModelProperty(value = "售后回调地址", name = "callBackApi")
    private String callBackApi;
    @ApiModelProperty(value = "回调参数", name = "callBackParam")
    private Map<String, Object> callBackParam;

    @ApiModelProperty(value = "是否退还优惠券")
    private Boolean refundCoupon;

    @ApiModelProperty(value = "店铺联系电话")
    private String mchPhone;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty("店铺信息")
    private MchModel mchModel;

    @ApiModelProperty("是否为供应商商品")
    private Boolean isSupplierPro;

    @ApiModelProperty("商品详情")
    private ProductListModel productListModel;

    @ApiModelProperty("订单详情状态")
    private Integer rStatus;

    @ApiModelProperty("实际退款金额")
    private BigDecimal realMoney;

    @ApiModelProperty("店铺退款详情记录")
    private MchAccountLogModel mchAccountLogModel;

    @ApiModelProperty("最大退款金额")
    private BigDecimal maxTuiMoney;

    @ApiModelProperty("是否未收货退款")
    private Boolean orderHadPayButNotReceiptFlag;

    @ApiModelProperty("是否已收货退款")
    private Boolean orderhadReceiptFlag;

    @ApiModelProperty("直播间id")
    private Integer livingRoomId;

    @ApiModelProperty("商品id")
    private Integer goodsId;

    @ApiModelProperty("属性id")
    private Integer attributeId;

    @ApiModelProperty("商品数量")
    private BigDecimal goodsNeedNum;

    @ApiModelProperty("购买用户id")
    private String clientUserId;

    @ApiModelProperty("剩余核销次数")
    private Integer afterWriteOffNum;

    @ApiModelProperty("订单金额")
    private BigDecimal orderPrice;

    @ApiModelProperty("优惠券id")
    private String couponId;

    @ApiModelProperty("父订单号")
    private String psNo;

    @ApiModelProperty("是否发送短信给商家")
    private Boolean isSendNotice;

    @ApiModelProperty("支付流水号")
    private String realOrderNo;

    @ApiModelProperty("调起支付金额")
    private BigDecimal upPaymentPrice;

    @ApiModelProperty("售后id")
    private Integer refundId;

    @ApiModelProperty("支付方式")
    private String oType;

    @ApiModelProperty("订单id")
    private Integer orderId;

    @ApiModelProperty("单个商品的实际支付金额")
    private BigDecimal goodsPayPrice;

    @ApiModelProperty("运费")
    private BigDecimal freight;

    @ApiModelProperty("是否结算")
    private Boolean isSettlement;

    @ApiModelProperty("买家名称")
    private String clientUserName;

    @ApiModelProperty("买家电话")
    private String clientUserPhon;

    @ApiModelProperty("核销次数")
    private Integer r_write_off_num;

    @ApiModelProperty("剩余退款金额")
    private BigDecimal remainMoney;

    @ApiModelProperty("是否为定时任务执行")
    private Boolean isTask = false;

    @ApiModelProperty("订单详情金额")
    private BigDecimal orderDetailPrice;

    @ApiModelProperty("插件id")
    private Integer p_id;
}
