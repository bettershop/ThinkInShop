package com.laiketui.common.service.dubbo.third;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.MchPromiseModel;
import com.laiketui.domain.mch.PromiseShModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.payment.PaymentConfigModel;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service("publicStripeServiceImpl")
public class PublicStripeServiceImpl implements PublicPaymentService
{
    private final Logger logger = LoggerFactory.getLogger(PublicStripeServiceImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private MchDistributionModelMapper mchDistributionModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private MchDistributionRecordModelMapper mchDistributionRecordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    /**
     * 创建全额退款
     *
     * @param paymentIntentId 支付意图ID
     * @return 退款对象
     * @throws StripeException 处理异常
     */
    public Refund createFullRefund(String paymentIntentId) throws StripeException
    {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        return Refund.create(params);
    }

    /**
     * 创建部分退款
     *
     * @param stripePaymentIntent 支付意图ID
     * @param amount              退款金额（以最小货币单位计，如美分）
     * @return 退款对象
     * @throws StripeException 处理异常
     */
    public Refund createPartialRefund(String stripePaymentIntent, Long amount) throws StripeException
    {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(stripePaymentIntent)
                .setAmount(amount)
                .build();
        return Refund.create(params);
    }

    /**
     * 查询退款状态
     *
     * @param refundId 退款ID
     * @return 退款对象
     * @throws StripeException 处理异常
     */
    public Refund retrieveRefund(String refundId) throws StripeException
    {
        return Refund.retrieve(refundId);
    }

    //店铺保证金退款
    @Override
    public Map<String, String> refundOrder(int storeId, PromiseShModel promiseShModel, String className, String tradeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            logger.info(">>stripe退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);
            Map<String, String> params = new HashMap<>(16);
            params.put("sNo", tradeNo);
            //商户订单号
            params.put("out_trade_no", tradeNo);
            //商户退款单号
            params.put("out_refund_no", tradeNo.concat(String.valueOf(System.currentTimeMillis())));
            //订单信息
            OrderModel orderModel = new OrderModel();
            //修改表
            promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
            PromiseShModel promiseShTKModel = promiseShModelMapper.selectByPrimaryKey(promiseShModel.getId());
            //根据审核表获取保证金表
            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            //修改保证金表
            MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
            mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
            mchPromiseUpdate.setIs_return_pay(1);
            mchPromiseUpdate.setId(mchPromiseOld.getId());
            mchPromiseUpdate.setUpdate_date(new Date());
            int row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }

            //存入stripe退款需要的参数
            OrderDataModel orderDataModel      = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
            String         orderDataModelData  = orderDataModel.getData();
            JSONObject     jsonObject          = JSON.parseObject(orderDataModelData);
            String         stripePaymentIntent = jsonObject.getString("stripe_payment_intent");
            logger.info("stripe_payment_intent：" + stripePaymentIntent);
            params.put("stripe_payment_intent", stripePaymentIntent);
            params.put("price", refundAmt.toString());
            params.put("store_id", String.valueOf(storeId));

            logger.info("stripe退款申请参数{}", params);
//            Map<String, String> resultMap1 = wxpay.refund(params);
            Map<String, Object> resultMap1 = refund(params);
            logger.info("#########退款信息#########start####");
            logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
            logger.info("#########退款信息#########end######");
            //stripe请求退款失败
            if (!"succeeded".equals(MapUtils.getString(resultMap1, "status")))
            {
                logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);

            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("stripe退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, int id, int isPass, String refusedWhy, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        return null;
    }

    //订单退款
    @Override
    public Map<String, String> refundOrder(int storeId, String sNo, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice, Integer refundId) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            logger.error(">>stripe退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);
            Map<String, String> params = new HashMap<>(16);
            params.put("sNo", sNo);
            params.put("price", refundAmt.toString());
            params.put("store_id", String.valueOf(storeId));

            //商户订单号
            params.put("out_trade_no", tradeNo);
            //售后id
            if (Objects.nonNull(refundId))
            {
                params.put("refundId", String.valueOf(refundId));
            }

            //订单信息
            OrderModel orderModel = new OrderModel();
            //商户退款单号
            if(!StringUtils.isEmpty(tradeNo))
            {
                params.put("out_refund_no", tradeNo.concat(String.valueOf(System.currentTimeMillis())));
                if (!isTempOrder)
                {
                    orderModel.setId(id);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    if (orderModel == null)
                    {
                        orderModel = new OrderModel();
                        orderModel.setReal_sno(tradeNo);
                        orderModel.setOtype(tradeNo.substring(0, 2));
                        logger.info("支付订单号：" + tradeNo);
                        orderModel = orderModelMapper.selectOne(orderModel);
                    }
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setR_sNo(orderModel.getsNo());
                    List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                    if (select.size() > 0)
                    {
                        orderDetailsModel = select.get(0);
                        proName = orderDetailsModel.getP_name();
                        if (select.size() > 1)
                        {
                            proName = proName + "等商品";
                        }
                    }
                }
                else
                {
                    OrderDataModel orderDataOld = orderDataModelMapper.selectByPrimaryKey(id);
                    if (orderDataOld == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    Map<String, Object> orderInfoData = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
                    {
                    });
                    orderModel.setUser_id(MapUtils.getString(orderInfoData, "user_id"));
                }
            }

            //如果是预售定金订单，需要分别使用定金和尾款的stripe id来进行退款操作，lkt_pre_sell_record查出两条记录则代表是定金模式
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS) && 2 == preSellRecordModelMapper.selectCountBySNo(sNo))
            {
                logger.error("进入stripe预售-定金模式退款方法");
                //定金金额
                BigDecimal deposit = preSellRecordModelMapper.selectDepositIdBySNoAndPayType(sNo, 0);
                //尾款金额
                BigDecimal balance = preSellRecordModelMapper.selectBalanceIdBySNoAndPayType(sNo, 1);

                // 分配退款金额
                BigDecimal refundDeposit; // 退定金金额
                BigDecimal refundBalance; // 退尾款金额


                if (refundAmt.compareTo(deposit) <= 0)
                {
                    //如果实际退款金额小于定金金额，则全部从定金中退款
                    refundDeposit = refundAmt;
                    //退定金
                    String stripePaymentIntent = preSellRecordModelMapper.selectStripePaymentIntentBySNoAndPayType(sNo, 0);
                    params.put("price", String.valueOf(refundDeposit));
                    params.put("stripe_payment_intent", stripePaymentIntent);
                    logger.info("stripe退预售定金进入refund前的params：" + params);
                    if (null != stripePaymentIntent)
                    {
                        logger.error("进入了预售退定金使用refund方法，stripe_payment_intent为：" + stripePaymentIntent + "退款金额为：" + refundDeposit);
                        Map<String, Object> resultMap1 = refund(params);
                        logger.error("预售退定金使用refund方法的返回值：" + resultMap1);

                    }
                }
                else
                {
                    // 如果实际退款金额大于定金金额，定金部分全额退款，剩余从尾款中退款
                    refundDeposit = deposit;
                    refundBalance = refundAmt.subtract(deposit);

                    //退定金
                    String stripePaymentIntent = preSellRecordModelMapper.selectStripePaymentIntentBySNoAndPayType(sNo, 0);
                    params.put("price", String.valueOf(refundDeposit));
                    params.put("stripe_payment_intent", stripePaymentIntent);
                    logger.info("stripe退预售定金进入refund前的params：" + params);
                    if (null != stripePaymentIntent)
                    {
                        logger.error("进入了预售退定金使用refund方法，stripe_payment_intent为：" + stripePaymentIntent + "退款金额为：" + refundDeposit);
                        Map<String, Object> resultMap1 = refund(params);
                        logger.error("预售退定金使用refund方法的返回值：" + resultMap1);

                    }
                    //退尾款
                    String stripePaymentIntent2 = preSellRecordModelMapper.selectStripePaymentIntentBySNoAndPayType(sNo, 1);
                    params.put("price", String.valueOf(refundBalance));
                    params.put("stripe_payment_intent", stripePaymentIntent2);
                    logger.info("stripe退预售尾款进入refund前的params：" + params);
                    if (null != stripePaymentIntent2)
                    {
                        logger.error("进入了预售退尾款使用refund方法，stripe_payment_intent_2为：" + stripePaymentIntent2 + "退款金额为：" + refundBalance);
                        Map<String, Object> resultMap1 = refund(params);
                        logger.error("预售退尾款使用refund方法的返回值：" + resultMap1);
                        //stripe请求退款失败
                        if (!"succeeded".equals(MapUtils.getString(resultMap1, "status")))
                        {
                            logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
                        }
                    }

                }


            }
            //拼团订单，stripe_payment_intent数据存在orderData表，需要提前传入stripe_payment_intent
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
            {
                //存入stripe退款需要的参数
                OrderDataModel orderDataModel = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
                logger.error("进入订单退款的refundOrder中的拼团订单退款，orderDataModel为" + orderDataModel.toString() + "tradeNo为：" + tradeNo);
                String     orderDataModelData  = orderDataModel.getData();
                JSONObject jsonObject          = JSON.parseObject(orderDataModelData);
                String     stripePaymentIntent = jsonObject.getString("stripe_payment_intent");
                logger.error("stripe_payment_intent来了：" + stripePaymentIntent);
                params.put("stripe_payment_intent", stripePaymentIntent);
                params.put("price", refundAmt.toString());
                logger.info("stripe退款申请具体参数{}", params);
                Map<String, Object> resultMap1 = refund(params);
                logger.info("#########退款信息#########start####");
                logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
                logger.info("#########退款信息#########end######");
                //stripe请求退款失败
                if (!"succeeded".equals(MapUtils.getString(resultMap1, "status")))
                {
                    logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
                }
            }
            else
            {
                logger.info("stripe退款申请具体参数{}", params);
                Map<String, Object> resultMap1 = refund(params);
                logger.info("#########退款信息#########start####");
                logger.error("退款信息，{}", JSONObject.toJSONString(resultMap1));
                logger.info("#########退款信息#########end######");
                //stripe请求退款失败
                if (!"succeeded".equals(MapUtils.getString(resultMap1, "status")))
                {
                    logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
                }
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);

            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("stripe退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    //竞拍保证金退款
    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            logger.error(">>stripe竞拍保证金退款或拼团失败退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);

            Map<String, String> params = new HashMap<>(16);
            params.put("sNo", tradeNo);
            params.put("price", refundAmt.toString());
            params.put("store_id", String.valueOf(storeId));

            //商户订单号
            params.put("out_trade_no", tradeNo);
            //商户退款单号
            params.put("out_refund_no", tradeNo.concat(String.valueOf(System.currentTimeMillis())));
            //订单信息
            OrderModel orderModel = new OrderModel();
            if (!isTempOrder)
            {
                orderModel.setId(id);
                orderModel = orderModelMapper.selectOne(orderModel);
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                if (orderModel == null)
                {
                    orderModel = new OrderModel();
                    orderModel.setReal_sno(tradeNo);
                    orderModel.setOtype(tradeNo.substring(0, 2));
                    logger.info("支付订单号：" + tradeNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                }
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setR_sNo(orderModel.getsNo());
                List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                if (select.size() > 0)
                {
                    orderDetailsModel = select.get(0);
                    proName = orderDetailsModel.getP_name();
                    if (select.size() > 1)
                    {
                        proName = proName + "等商品";
                    }
                }
            }
            else
            {
                OrderDataModel orderDataOld = orderDataModelMapper.selectByPrimaryKey(id);
                if (orderDataOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                Map<String, Object> orderInfoData = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
                {
                });
                orderModel.setUser_id(MapUtils.getString(orderInfoData, "user_id"));
            }

            if (tradeNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB) || tradeNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                //将stripePaymentIntent存到params
                OrderDataModel orderDataByTradeNo  = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
                String         data                = orderDataByTradeNo.getData();
                JSONObject     dataJson            = JSONObject.parseObject(data);
                Object         stripePaymentIntent = dataJson.get("stripe_payment_intent");
                params.put("stripe_payment_intent", (String) stripePaymentIntent);
                logger.error("stripe-竞拍保证金退款或开团失败退款 已将stripe_payment_intent存入params" + params.toString());
            }

            logger.info("stripe竞拍保证金退款或开团失败退款申请参数{}", params);
            Map<String, Object> resultMap1 = refund(params);
            logger.info("#########退款信息#########start####");
            logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
            logger.info("#########退款信息#########end######");
            //请求退款失败
            if (!"succeeded".equals(MapUtils.getString(resultMap1, "status")))
            {
                logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "err_code_des"));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "err_code_des"), "refundOrder");
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("stripe退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }


    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        String sNo = (String) params.get("sNo");

        logger.error("refund方法的sNo为：" + sNo);
        logger.info("refund方法中的params参数：" + params.toString());
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        //根据订单信息获取stripe支付配置信息
        paymentConfigModel.setPid(16);
        if (params.get("store_id") != null)
        {
            paymentConfigModel.setStore_id(Integer.parseInt(params.get("store_id").toString()));
        }
        else
        {
            paymentConfigModel.setStore_id(1);
        }
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        if(paymentConfigModel == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ,"当前支付方式Stripe未配置","refund");
        }

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });
        //stripe密钥
        String secretKey = MapUtils.getString(param, "secret_key");

        if (!org.apache.commons.lang3.StringUtils.isEmpty(secretKey))
        {
            param.put("secret_key", secretKey.replaceAll("%2B", "\\+"));
        }

        //获取订单数据，从而拿到stripe支付意图id stripePaymentIntent
        OrderModel orderModel          = orderModelMapper.selectBySno(MapUtils.getString(params, "sNo"));
        String     stripePaymentIntent = null;
        if (orderModel != null)
        {
            stripePaymentIntent = orderModel.getStripe_payment_intent();
            logger.error("退款使用的stripePaymentIntent：{}", stripePaymentIntent);
            //TODO 无订单的场景需要单独调试修改代码
            params.put("currency_code", orderModel.getCurrency_code());
            params.put("exchange_rate", orderModel.getExchange_rate());
        }
        else
        {
            //退店铺保证金的情况下，没有orderModel，lkt_order表里没有对应数据，这时stripe_payment_intent是提前放入params传进来的
            logger.info("ordermodel为空");
            stripePaymentIntent = MapUtils.getString(params, "stripe_payment_intent");
            logger.info("ordermodel为空后设置的stripePaymentIntent:" + stripePaymentIntent);
        }

        //预售-定金模式情况下，stripe_payment_intent存在lkt_pre_sell_record表，这时stripe_payment_intent是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS) && 2 == preSellRecordModelMapper.selectCountBySNo(sNo))
        {
            stripePaymentIntent = MapUtils.getString(params, "stripe_payment_intent");
            logger.error("预售-定金模式下，从lkt_pre_sell_record表查询到的stripe_payment_intent是：" + stripePaymentIntent);
        }

        //退竞拍保证金时，stripe_payment_intent是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
        {
            stripePaymentIntent = MapUtils.getString(params, "stripe_payment_intent");
            logger.error("退竞拍保证金时stripe_payment_intent是：" + stripePaymentIntent);
        }

        //拼团订单退款时，stripe_payment_intent是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
        {
            stripePaymentIntent = MapUtils.getString(params, "stripe_payment_intent");
            logger.error("拼团订单退款时stripe_payment_intent是：" + stripePaymentIntent);
        }


        //第三方（stripe）退款具体操作

        try
        {
            //设置stripe密钥
            Stripe.apiKey = secretKey;
            // 获取以元为单位的金额
            Double priceInYuan = MapUtils.getDouble(params, "price");
            if (priceInYuan == null || priceInYuan <= 0)
            {
                throw new IllegalArgumentException("无效的退款金额");
            }
            // 转换为分（乘以100并取整）
            Long priceInFen = Math.round(priceInYuan * 100);
            // 执行退款请求 传入stripe支付id、退款金额 注意退款金额以分为单位（金额已转换为分）
            Refund refund = createPartialRefund(stripePaymentIntent, priceInFen);
            logger.error("stripe退款返回值：{}", refund.toJson());

            //        退款返回值处理
            param.put("status", refund.getStatus());
        }
        catch (StripeException e)
        {
            logger.error("stripe退款失败，失败原因：{}", e.getMessage());
            throw new RuntimeException(e);
        }


        return param;
    }

    @Override
    public String qrCodeOrder(String appid, String privateKey, String publicKey, String orderno, BigDecimal orderAmt) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt, BigDecimal orderPrice) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        return null;
    }
}
