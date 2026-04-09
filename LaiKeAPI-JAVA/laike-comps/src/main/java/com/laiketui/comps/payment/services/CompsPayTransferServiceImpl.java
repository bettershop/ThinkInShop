package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.MessageListModelMapper;
import com.laiketui.common.mapper.OrderDataModelMapper;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.service.dubbo.third.PublicPaypalServiceImpl;
import com.laiketui.comps.api.payment.CompsPayService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.order.OrderModel;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.API_OPERATION_FAILED;

/**
 * 支付中转
 *
 * @author Trick
 * @date 2022/8/15 10:24
 */
@Service("payTransferService")
public class CompsPayTransferServiceImpl extends CompsPayServiceAdapter implements CompsPayService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicPaypalServiceImpl publicPaypalService;

    @Autowired
    @Qualifier("publicStripeServiceImpl")
    private PublicPaymentService publicStripePayService;

    @Autowired
    private MessageListModelMapper messageListModelMapper;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService publicAlipayService;
    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicWechatService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;


    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        return super.pay(params);
    }

    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            logger.info("退款参数-{}-,", JSON.toJSONString(params));
            //支付方式
            String payType = MapUtils.getString(params, "payType");
            //商城id
            Integer storeId = MapUtils.getInteger(params, "storeId");
            //购买者userId
            String userId = MapUtils.getString(params, "userId");
            //订单id
            Integer oid = MapUtils.getInteger(params, "oid");
            //订单号
            String orderNo = MapUtils.getString(params, "sNo");

            if (params.containsKey("orderNo"))
            {
                orderNo = MapUtils.getString(params, "orderNo");
                OrderModel orderModel = new OrderModel();
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                if (orderModel == null)
                {
                    logger.error("退款失败订单信息{}", JSON.toJSONString(orderModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                }
                //退款订单
                orderNo = orderModel.getReal_sno();
            }
            //退款金额
            BigDecimal refundAmt = new BigDecimal(MapUtils.getString(params, "refundAmt"));
            //是否是订单临时表
            boolean isTempOrder = MapUtils.getBooleanValue(params, "isTempOrder");
            //退款流程
            switch (payType)
            {
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                    //钱包支付全额退款
                    publicMemberService.returnUserMoney(storeId, userId, refundAmt, oid, isTempOrder);
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                    //发起退款
                    Map<String, String> map = publicAlipayService.refundOrder(storeId, 0, payType, orderNo, refundAmt, refundAmt);
                    String code = map.get("code");
                    if (DictionaryConst.AliApiCode.ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH.equals(code))
                    {
                        logger.error("支付宝退款失败 卖家余额不足 code:{}", code);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                    }
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                    //微信退款
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", orderNo);
                    map = publicWechatService.refundOrder(storeId, oid, payType, orderNo, refundAmt, isTempOrder, refundAmt);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                //贝宝退款
                case DictionaryConst.OrderPayType.PAYPAL_PAY:
                    logger.error("进入贝宝退款逻辑，params为-{},", JSON.toJSONString(params));
                    logger.info("退款参数-{}-,", JSON.toJSONString(params));
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", orderNo);
                    map = publicPaypalService.refundOrder(storeId, oid, payType, orderNo, refundAmt, isTempOrder, refundAmt);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                //stripe退款
                case DictionaryConst.OrderPayType.STRIPE_PAY:
                    logger.error("退款参数-{}-,", JSON.toJSONString(params));
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", orderNo);
                    map = publicStripePayService.refundOrder(storeId, oid, payType, orderNo, refundAmt, isTempOrder, refundAmt);
                    logger.error("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;

                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在", "refund");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("支付中转失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("支付退款中转异常", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信退款回调失败", "payBack");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        return super.payBack(params);
    }

    @Override
    public Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException
    {
        return super.getPayConfig(params);
    }
}
