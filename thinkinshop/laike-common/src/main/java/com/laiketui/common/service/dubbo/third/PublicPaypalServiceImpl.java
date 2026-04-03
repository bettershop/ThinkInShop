package com.laiketui.common.service.dubbo.third;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.paypal.PayPalClient;
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
import com.paypal.http.HttpResponse;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.payments.CapturesRefundRequest;
import com.paypal.payments.RefundRequest;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("publicPaypalServiceImpl")
public class PublicPaypalServiceImpl implements PublicPaymentService
{

    public static final String CAPTURE            = "CAPTURE";
    /**
     * 该标签将覆盖PayPal网站上PayPal帐户中的公司名称
     */
    public static final String BRANDNAME          = "来客推";
    /**
     * LOGIN。当客户单击PayPal Checkout时，客户将被重定向到页面以登录PayPal并批准付款。
     * BILLING。当客户单击PayPal Checkout时，客户将被重定向到一个页面，以输入信用卡或借记卡以及完成购买所需的其他相关账单信息
     * NO_PREFERENCE。当客户单击“ PayPal Checkout”时，将根据其先前的交互方式将其重定向到页面以登录PayPal并批准付款，或重定向至页面以输入信用卡或借记卡以及完成购买所需的其他相关账单信息使用PayPal。
     * 默认值：NO_PREFERENCE
     */
    public static final String LANDINGPAGE        = "NO_PREFERENCE";
    /**
     * CONTINUE。将客户重定向到PayPal付款页面后，将出现“ 继续”按钮。当结帐流程启动时最终金额未知时，请使用此选项，并且您想将客户重定向到商家页面而不处理付款。
     * PAY_NOW。将客户重定向到PayPal付款页面后，出现“ 立即付款”按钮。当启动结帐时知道最终金额并且您要在客户单击“ 立即付款”时立即处理付款时，请使用此选项。
     */
    public static final String USERACTION         = "PAY_NOW";
    /**
     * GET_FROM_FILE。使用贝宝网站上客户提供的送货地址。
     * NO_SHIPPING。从PayPal网站编辑送货地址。推荐用于数字商品
     * SET_PROVIDED_ADDRESS。使用商家提供的地址。客户无法在PayPal网站上更改此地址
     */
    public static final String SHIPPINGPREFERENCE = "SET_PROVIDED_ADDRESS";

    public static final String MODE = "sandbox";

    private final Logger logger = LoggerFactory.getLogger(PublicPaypalServiceImpl.class);

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


    //店铺保证金退款
    @Override
    public Map<String, String> refundOrder(int storeId, PromiseShModel promiseShModel, String className, String tradeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            logger.info(">>贝宝退款开始>>");
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

            //存入贝宝退款需要的参数
            OrderDataModel orderDataModel     = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
            String         orderDataModelData = orderDataModel.getData();
            JSONObject     jsonObject         = JSON.parseObject(orderDataModelData);
            String         paypal_id          = jsonObject.getString("paypal_id");
            logger.info("paypal_id来了：" + paypal_id);
            params.put("paypal_id", paypal_id);
            params.put("price", refundAmt.toString());

            logger.info("贝宝退款申请参数{}", params);
//            Map<String, String> resultMap1 = wxpay.refund(params);
            Map<String, Object> resultMap1 = refund(params);
            logger.info("#########退款信息#########start####");
            logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
            logger.info("#########退款信息#########end######");
            //微信请求退款失败
            if (!"COMPLETED".equals(MapUtils.getString(resultMap1, "status")))
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
            logger.error("贝宝退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    //订单退款
    @Override
    public Map<String, String> refundOrder(int storeId, String sNo, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice,Integer refundId) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            logger.info(">>贝宝退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);
            Map<String, String> params = new HashMap<>(16);
            params.put("sNo", sNo);
            params.put("price", refundAmt.toString());

            //商户订单号
            params.put("out_trade_no", tradeNo);
            //售后id
            if (Objects.nonNull(refundId))
            {
                params.put("refundId",String.valueOf(refundId));
            }
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


            //如果是预售定金订单，需要分别使用定金和尾款的贝宝id来进行退款操作，lkt_pre_sell_record查出两条记录则代表是定金模式
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS) && 2 == preSellRecordModelMapper.selectCountBySNo(sNo))
            {
                logger.error("进入贝宝预售-定金模式退款方法");
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
                    String paypalId1 = preSellRecordModelMapper.selectPaypalIdBySNoAndPayType(sNo, 0);
                    params.put("price", String.valueOf(refundDeposit));
                    params.put("paypal_id", paypalId1);
                    logger.info("贝宝退预售定金进入refund前的params：" + params);
                    if (null != paypalId1)
                    {
                        logger.error("进入了预售退定金使用refund方法，paypalId1为：" + paypalId1 + "退款金额为：" + refundDeposit);
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
                    String paypalId1 = preSellRecordModelMapper.selectPaypalIdBySNoAndPayType(sNo, 0);
                    params.put("price", String.valueOf(refundDeposit));
                    params.put("paypal_id", paypalId1);
                    logger.info("贝宝退预售定金进入refund前的params：" + params);
                    if (null != paypalId1)
                    {
                        logger.error("进入了预售退定金使用refund方法，paypalId1为：" + paypalId1 + "退款金额为：" + refundDeposit);
                        Map<String, Object> resultMap1 = refund(params);
                        logger.error("预售退定金使用refund方法的返回值：" + resultMap1);

                    }
                    //退尾款
                    String paypalId2 = preSellRecordModelMapper.selectPaypalIdBySNoAndPayType(sNo, 1);
                    params.put("price", String.valueOf(refundBalance));
                    params.put("paypal_id", paypalId2);
                    logger.info("贝宝退预售尾款进入refund前的params：" + params);
                    if (null != paypalId2)
                    {
                        logger.error("进入了预售退尾款使用refund方法，paypalId2为：" + paypalId2 + "退款金额为：" + refundBalance);
                        Map<String, Object> resultMap1 = refund(params);
                        logger.error("预售退尾款使用refund方法的返回值：" + resultMap1);
                        //贝宝请求退款失败
                        if (!"COMPLETED".equals(MapUtils.getString(resultMap1, "status")))
                        {
                            logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
                        }
                    }

                }


            }
            //拼团订单，贝宝id数据存在orderData表，需要提前传入贝宝id
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
            {
                //存入贝宝退款需要的参数
                OrderDataModel orderDataModel = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
                logger.error("进入订单退款的refundOrder中的拼团订单退款，orderDataModel为" + orderDataModel.toString() + "tradeNo为：" + tradeNo);
                String     orderDataModelData = orderDataModel.getData();
                JSONObject jsonObject         = JSON.parseObject(orderDataModelData);
                String     paypal_id          = jsonObject.getString("paypal_id");
                logger.error("paypal_id来了：" + paypal_id);
                params.put("paypal_id", paypal_id);
                params.put("price", refundAmt.toString());
                logger.info("贝宝退款申请具体参数{}", params);
                Map<String, Object> resultMap1 = refund(params);
                logger.info("#########退款信息#########start####");
                logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
                logger.info("#########退款信息#########end######");
                //贝宝请求退款失败
                if (!"COMPLETED".equals(MapUtils.getString(resultMap1, "status")))
                {
                    logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "status_code"));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "status_code"), "refundOrder");
                }
            }
            else
            {
                logger.info("贝宝退款申请具体参数{}", params);
                Map<String, Object> resultMap1 = refund(params);
                logger.info("#########退款信息#########start####");
                logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
                logger.info("#########退款信息#########end######");
                //贝宝请求退款失败
                if (!"COMPLETED".equals(MapUtils.getString(resultMap1, "status")))
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
            logger.error("贝宝退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    /**
     * 创建退款请求体
     */
    public RefundRequest buildRefundRequestBody(Map map)
    {
        RefundRequest             refundRequest = new RefundRequest();
        com.paypal.payments.Money money         = new com.paypal.payments.Money();
        String currenCode = MapUtils.getString(map, "currency_code");
        if (StringUtils.isNotEmpty(currenCode))
        {
            money.currencyCode(currenCode);
        }
        else
        {
            // todo 币种如果没有的情况直接抛异常 【理论上不存在这个场景】目前除了正价商品之外其他场景需要走这个分支
            money.currencyCode("USD");
        }

        BigDecimal price        = new BigDecimal(MapUtils.getDouble(map, "price"));
        Double     exchangeRate = MapUtils.getDouble(map, "exchange_rate");
        if (exchangeRate != null)
        {
            money.value(price.multiply(new BigDecimal(exchangeRate)).toString());
        }
        else
        {
            // todo 币种汇率如果没有的情况直接抛异常 【理论上不存在这个场景】目前除了正价商品之外其他场景需要走这个分支
            money.value(String.valueOf(price.doubleValue()));
        }

        refundRequest.amount(money);
//        refundRequest.invoiceId("T202005230002");
        refundRequest.noteToPayer("申请退款");
        return refundRequest;
    }

    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        String sNo = (String) params.get("sNo");

        logger.error("refund方法的sNo为：" + sNo);
        logger.info("refund方法中的params参数：" + params.toString());
        //获取品牌名称 贝宝客户端id 密钥 brandName clientId clientSecret
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        //根据订单信息获取贝宝支付配置信息
        paymentConfigModel.setPid(15);
        paymentConfigModel.setStore_id(1);
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });
        String brandName = MapUtils.getString(param, "brand_name");
        String clientId  = MapUtils.getString(param, "client_id");
        if (!StringUtils.isEmpty(clientId))
        {
            param.put("client_id", clientId.replaceAll("%2B", "\\+"));
        }
        clientId = MapUtils.getString(param, "client_id");
        String clientSecret = MapUtils.getString(param, "client_secret");
        if (!StringUtils.isEmpty(clientSecret))
        {
            param.put("client_secret", clientSecret.replaceAll("%2B", "\\+"));
        }
        clientSecret = MapUtils.getString(param, "client_secret");

        //获取订单数据，从而拿到贝宝订单id
        OrderModel orderModel = orderModelMapper.selectBySno(MapUtils.getString(params, "sNo"));
        String     paypalId   = null;
        if (orderModel != null)
        {
            paypalId = orderModel.getPaypal_id();
            logger.error("退款使用的贝宝id：{}", paypalId);
            //TODO 无订单的场景需要单独调试修改代码
            params.put("currency_code", orderModel.getCurrency_code());
            params.put("exchange_rate", orderModel.getExchange_rate());
        }
        else
        {
            //退店铺保证金的情况下，没有orderModel，lkt_order表里没有对应数据，这时paypal_id是提前放入params传进来的
            logger.info("ordermodel为空");
            paypalId = MapUtils.getString(params, "paypal_id");
            logger.info("ordermodel为空后设置的paypalId:" + paypalId);
        }

        //预售-定金模式情况下，paypal_id存在lkt_pre_sell_record表，这时paypal_id是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS) && 2 == preSellRecordModelMapper.selectCountBySNo(sNo))
        {
            paypalId = MapUtils.getString(params, "paypal_id");
//            paypalId = preSellRecordModelMapper.getBySNo(MapUtils.getString(params, "sNo"));
            logger.error("预售-定金模式下，从lkt_pre_sell_record表查询到的paypalId是：" + paypalId);
        }

        //退竞拍保证金时，paypal_id是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
        {
            paypalId = MapUtils.getString(params, "paypal_id");
            logger.error("退竞拍保证金时paypalId是：" + paypalId);
        }

        //拼团订单退款时，paypal_id是提前放入params传进来的
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
        {
            paypalId = MapUtils.getString(params, "paypal_id");
            logger.error("拼团订单退款时paypalId是：" + paypalId);
        }



        OrdersGetRequest    ordersGetRequest  = new OrdersGetRequest(paypalId);
        PayPalClient        payPalClient      = new PayPalClient();
        HttpResponse<Order> ordersGetResponse = null;
        try
        {
            ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
        }
        catch (Exception e)
        {
            try
            {
                logger.error("第1次调用paypal订单查询失败");
                ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
            }
            catch (Exception e2)
            {
                try
                {
                    logger.error("第2次调用paypal订单查询失败");
                    ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
                }
                catch (Exception e3)
                {
                    logger.error("第3次调用paypal订单查询失败，失败原因：{}", e3.getMessage());
                }
            }
        }
        String                captureId = ordersGetResponse.result().purchaseUnits().get(0).payments().captures().get(0).id();
        CapturesRefundRequest request   = new CapturesRefundRequest(captureId);
        request.prefer("return=representation");
        request.requestBody(buildRefundRequestBody(params));
        HttpResponse<com.paypal.payments.Refund> response = null;
        try
        {
            response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
        }
        catch (IOException e)
        {
            try
            {
                logger.error("第1次调用paypal退款申请失败{}",e.getMessage());
                response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
            }
            catch (Exception e1)
            {
                try
                {
                    logger.error("第2次调用paypal退款申请失败{}",e1.getMessage());
                    response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
                }
                catch (Exception e2)
                {
                    logger.error("第3次调用paypal退款申请失败，失败原因 {}", e2.getMessage());
                }
            }
        }
        //把退款返回值存入map并返回
        param.put("status_code", response.statusCode());
        param.put("status", response.result().status());
        String payPalRefundId = response.result().id();
        if (params.containsKey("refundId"))
        {
            logger.info("贝宝退款id：：：{}",payPalRefundId);
            logger.info("将贝宝退款id存入售后表(定时任务查询退款情况做后续的业务)");
            //将贝宝退款id存入售后表（定时任务查询退款情况做后续的业务）
            int refundId = Integer.parseInt(MapUtils.getString(params, "refundId"));
            logger.info("售后id：：：：{}",refundId);
            returnOrderModelMapper.setPayPalRefundId(refundId, payPalRefundId);
        }

        logger.info("Status Code = {}, Status = {}, RefundID = {}", response.statusCode(), response.result().status(), response.result().id());
        if ("COMPLETED".equals(response.result().status()))
        {
            //进行数据库操作，修改状态为已退款（配合回调和退款查询确定退款成功）
            logger.info("退款成功");
        }
        for (com.paypal.payments.LinkDescription link : response.result().links())
        {
            logger.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
        }
//        String json = new JSONObject(new Json().serialize(response.result())).toString(4);
//        logger.info("refundOrder response body: {}", json);
//        return response;

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


    @Override
    public Map<String, String> refundOrder(int storeId, int id, int isPass, String refusedWhy, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        return null;
    }

    //竞拍保证金退款
    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            logger.error(">>贝宝竞拍保证金退款或拼团失败退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);

            Map<String, String> params = new HashMap<>(16);
            params.put("sNo", tradeNo);
            params.put("price", refundAmt.toString());

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
                //将贝宝id存到params
                OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(tradeNo);
                String         data               = orderDataByTradeNo.getData();
                JSONObject     dataJson           = JSONObject.parseObject(data);
                Object         paypal_id          = dataJson.get("paypal_id");
                params.put("paypal_id", (String) paypal_id);
                logger.error("贝宝-竞拍保证金退款或开团失败退款 已将paypal_id存入params" + params.toString());
            }

            logger.info("贝宝竞拍保证金退款或开团失败退款申请参数{}", params);
            Map<String, Object> resultMap1 = refund(params);
            logger.info("#########退款信息#########start####");
            logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
            logger.info("#########退款信息#########end######");
            //请求退款失败
            if (!"COMPLETED".equals(MapUtils.getString(resultMap1, "status")))
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
            logger.error("贝宝退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder) throws LaiKeAPIException
    {
        return PublicPaymentService.super.refundOrder(storeId, id, className, tradeNo, refundAmt, isTempOrder);
    }
}
