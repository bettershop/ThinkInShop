package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.payment.CompsWalletPayService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.plugin.group.PtGoGroupOrderModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;

/**
 * @author wangxian
 */
@Service
public class CompsWalletPayServiceDubboImpl extends CompsPayServiceAdapter implements CompsWalletPayService
{

    private final Logger logger = LoggerFactory.getLogger(CompsWalletPayServiceDubboImpl.class);

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private GoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private PtGoGroupOrderModelMapper ptGoGroupOrderModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicPaymentService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            if (params == null || params.size() == 0)
            {
                throw new LaiKeAPIException(DATA_ILLEGAL_FAIL, "参数错误", "pay");
            }
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            //模块标识
            String loginTokenKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
            User   user;
            if (GloabConst.StoreType.STORE_TYPE_PC_MALL == paymentVo.getStoreType())
            {
                logger.debug("pc商城支付");
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, loginTokenKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            }
//            publicUserService.validatePayPwd(user.getUser_id(), paymentVo.getPassword());
            // 商城id
            int storeId = paymentVo.getStoreId();
            // 参数
            //{"total":2,"sNo":"GM20210308182405400402","order_id":1453}
            String     orderList  = paymentVo.getOrder_list();
            JSONObject jsonObject = JSONObject.parseObject(orderList);
            // 订单sNo
            String sNo;
            if (!StringUtils.isEmpty(paymentVo.getsNo()))
            {
                sNo = paymentVo.getsNo();
            }
            else
            {
                sNo = jsonObject.getString("sNo");
            }
            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
            }
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            if (sNo.contains(DictionaryConst.OrdersType.PTHD_ORDER_PP))
            {
                PtGoGroupOrderModel ptGoGroupOrderModel = new PtGoGroupOrderModel();
                ptGoGroupOrderModel.setsNo(sNo);
                ptGoGroupOrderModel = ptGoGroupOrderModelMapper.selectOne(ptGoGroupOrderModel);
                orderModel = JSON.parseObject(JSON.toJSONString(ptGoGroupOrderModel), OrderModel.class);
            }
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setsNo(sNo);
                preSellRecordModel.setIs_pay(0);
                List<PreSellRecordModel> preSellRecordModelList = preSellRecordModelMapper.select(preSellRecordModel);
                Integer payTarget = null;
                if (CollectionUtils.isNotEmpty(preSellRecordModelList))
                {
                    for (PreSellRecordModel sellRecordModel : preSellRecordModelList)
                    {
                        //支付类型 0.定金 1.尾款
                        Integer payType = sellRecordModel.getPay_type();

                        if (Objects.isNull(payType))
                        {
                            payTarget = 3;
                        }else
                        {
                           payTarget = payType == 0 ? 1 : 2;
                        }
                    }
                }
                paymentVo.setPayTarget(payTarget);
                if (!Objects.isNull(paymentVo.getPayTarget()))
                {
                    if (paymentVo.getPayTarget() != 1)
                    {
                        if (paymentVo.getPayTarget() == 2)
                        {
                            preSellRecordModel = new PreSellRecordModel();
                            preSellRecordModel.setsNo(sNo);
                            preSellRecordModel.setPay_type(PreSellRecordModel.BALANCE);
                            preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                            if (preSellRecordModel != null)
                            {
                                preSellRecordModel.setPay(paymentVo.getPayType());
                                preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel);
                                orderModel.setsNo(sNo);
                                //尾款总金额看后面代码是直接设定为前端传来的支付金额，总金额是包含运费在内的与z_price不同
                                orderModel.setOld_total(new BigDecimal(paymentVo.getPayment_money()));
                                orderModel.setZ_price(preSellRecordModel.getBalance());
                                orderModel.setReal_sno(preSellRecordModel.getReal_sno());
                                orderModel.setStore_id(preSellRecordModel.getStore_id());
                                orderModel.setUser_id(preSellRecordModel.getUser_id());
                            }
                        }
                        else
                        {
                            orderModel.setsNo(sNo);
                            orderModel = orderModelMapper.selectOne(orderModel);
                        }
                    }
                    else
                    {
                        preSellRecordModel = new PreSellRecordModel();
                        preSellRecordModel.setsNo(sNo);
                        preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                        preSellRecordModel.setPay(paymentVo.getPayType());
                        preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel);
                        orderModel = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                    }
                }
            }
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(sNo);
                OrderDataModel orderData = orderDataModelMapper.selectOne(orderDataModel);
                if (Objects.isNull(orderData))
                {
                    throw new LaiKeAPIException(DATA_NOT_EXIST, "订单不存在", "pay");
                }
                MemberOrderVo memberOrderVo = JSONObject.parseObject(orderData.getData(), MemberOrderVo.class);
                resultMap = publicOrderService.payBackForMember(orderData);
                publicPaymentService.walletPay(user.getUser_id(), memberOrderVo.getAmount(), paymentVo.getAccessId(), loginTokenKey, sNo);
                return resultMap;
            }
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                logger.info("开始支付保证金流程");
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(sNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderMchPromise(orderDataModel);
                return resultMap;
            }/*else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB)) {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(sNo);
                OrderDataModel orderData = orderDataModelMapper.selectOne(orderDataModel);
                if (Objects.isNull(orderData)) {
                    throw new LaiKeAPIException(DATA_NOT_EXIST, "订单不存在", "pay");
                }
                PromiseOrderVo promiseOrderVo = JSON.parseObject(orderDataModel.getData(), PromiseOrderVo.class);
                resultMap = publicOrderService.payBackUpOrderAuctionPromise(orderDataModel);
                publicPaymentService.walletPay(user.getUser_id(), promiseOrderVo.getPaymentAmt(), paymentVo.getAccessId(), loginTokenKey);
                return resultMap;
            }*/
            else
            {
                if (StringUtils.isNotEmpty(paymentVo.getParameter()))
                {
                    //是否有内部api参数,如果有则调用内部实现 add by trick 2022-08-09 17:32:34
                    try
                    {
                        logger.debug("钱包支付回parameter调参数:{}", paymentVo.getParameter());
                        Map<String, Object> paramMap = JSON.parseObject(paymentVo.getParameter(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                        if (StringUtils.isNotEmpty(laikeApiUrl))
                        {
                            logger.debug("正在支付订单{} 执行接口:{}", sNo, laikeApiUrl);
                            if ("plugin.auction.payPromise".equals(laikeApiUrl) || "plugin.auction.payCallBack".equals(laikeApiUrl))
                            {
                                Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(paymentVo));
                                resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                                BigDecimal payment = new BigDecimal(MapUtils.getString(resultMap, "payment"));
                                publicPaymentService.walletPay(user.getUser_id(), payment, paymentVo.getAccessId(), loginTokenKey, sNo);
                                return resultMap;
                            }
                            else
                            {
                                Map<String, Object> paramMap1 = new HashMap<>(1);
                                paramMap1.put("vo", JSON.toJSONString(paymentVo));
                                resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                                //resultMap = httpApiUtils.executeApi(laikeApiUrl, JSON.toJSONString(paramMap1));
                                BigDecimal payment = new BigDecimal(MapUtils.getString(resultMap, "payment"));
                                publicPaymentService.walletPay(user.getUser_id(), payment, paymentVo.getAccessId(), loginTokenKey, sNo);
                                return resultMap;
                            }
                        }
                        throw new LaiKeAPIException(PARAMATER_ERROR, "参数错误", "pay");
                    }
                    catch (JSONException json)
                    {
                        orderModel.setsNo(sNo);
                        orderModel = orderModelMapper.selectOne(orderModel);
                    }
                }
                else
                {
                    orderModel.setsNo(sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                }
            }

            if (Objects.isNull(orderModel))
            {
                throw new LaiKeAPIException(DATA_NOT_EXIST, "订单不存在", "pay");
            }
            if (orderModel.getStatus() != null && !orderModel.getStatus().equals(OrderModel.ORDER_UNPAY))
            {
                List<OrderModel> orderModelList = null;
                //订单是否多店铺已经拆单
                if (orderModel.getStatus().equals(OrderModel.ORDER_CLOSE))
                {
                    OrderModel order = new OrderModel();
                    order.setStore_id(storeId);
                    order.setP_sNo(sNo);
                    orderModelList = orderModelMapper.select(order);
                }
                if (orderModelList == null)
                {
                    throw new LaiKeAPIException(ERROR_CODE_DDYZF, "该订单已支付", "pay");
                }
            }

            // 会员特惠 兑换券级别
            int grade = paymentVo.getGrade();
            //支付总金额
            BigDecimal payTotal = orderModel.getOld_total();
            // 余额抵扣金额
            if (payTotal.compareTo(BigDecimal.ZERO) > 0 && grade != 0)
            {
                if (payTotal.compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new LaiKeAPIException(AMOUNT_ERROR, "金额错误", "pay");
                }
            }
            //支付成功,立即回调
            orderModel.setPay(paymentVo.getPayType());
            if (orderModel.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                logger.info("开始支付预售订单流程");
                resultMap = publicOrderService.payBackForPreSell(paymentVo, orderModel);
                publicPaymentService.walletPay(user.getUser_id(), new BigDecimal(paymentVo.getPayment_money()), paymentVo.getAccessId(), loginTokenKey, sNo);
            }
            else
            {
                logger.info("#################");
                publicPaymentService.walletPay(user.getUser_id(), payTotal, paymentVo.getAccessId(), loginTokenKey, sNo);
                resultMap = publicOrderService.payBackUpOrder(orderModel);
                logger.info(">>>>>>>>>>>>>>>>>>>>");
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("钱包支付异常 ", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("钱包支付异常 ", e);
            e.printStackTrace();
            throw new LaiKeAPIException(ERROR_CODE_QBZFSB, "钱包支付失败", "pay");
        }
        return resultMap;
    }

    /**
     * 钱包退款
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            String     userId = MapUtils.getString(params, "userId", "");
            BigDecimal price  = new BigDecimal(MapUtils.getString(params, "price", "0"));
            User       user   = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);

            publicPaymentService.walletReturnPay(user.getUser_id(), price, user.getAccess_token());
        }
        catch (LaiKeAPIException e)
        {
            logger.error("钱包退款失败 异常", e);
            throw new LaiKeAPIException("", "钱包退款失败", "pay");
        }
        return resultMap;
    }
}