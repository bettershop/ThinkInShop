package com.laiketui.comps.payment.services;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.payment.CompsPaypalPayService;
import com.laiketui.comps.payment.util.CompsPayPalClient;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchPromiseModel;
import com.laiketui.domain.mch.PromiseRecordModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.payment.PaymentConfigModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.root.common.BuilderIDTool;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.paypal.payments.CapturesRefundRequest;
import com.paypal.payments.RefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;
import static com.laiketui.core.lktconst.GloabConst.StoreType.STORE_TYPE_PC_MALL;

@Slf4j
@Service("compsPaypalPayServiceImpl")
@RefreshScope
public class CompsPaypalPayServiceImpl extends CompsPayServiceAdapter implements CompsPaypalPayService
{

    private final Logger logger = LoggerFactory.getLogger(CompsPaypalPayServiceImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private PromiseRecordModelMapper promiseRecordModelMapper;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;


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


    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        PaymentVo           paymentVo = (PaymentVo) params.get("paymentVo");
        User user = RedisDataTool.getLktUser(paymentVo.getAccessId(), redisUtil);
        logger.info("传入信息：{}", params);
        logger.error("传入信息PaymentVo：{}", JSON.toJSONString(paymentVo));
        String payType = paymentVo.getType();
        String sNo     = (String) params.get("sNo");
        if (StringUtils.isEmpty(sNo))
        {
            String     orderList  = paymentVo.getOrder_list();
            JSONObject jsonObject = JSONObject.parseObject(orderList);
            if (!com.laiketui.core.utils.tool.StringUtils.isEmpty(paymentVo.getsNo()))
            {
                sNo = paymentVo.getsNo();
            }
            else
            {
                sNo = jsonObject.getString("sNo");
                paymentVo.setsNo(sNo);
            }
        }
        logger.info("sNo为：" + sNo);
        logger.info("params信息：" + params.toString());
        logger.info("paymentVo:" + paymentVo.toString());

        //从config表拿本商城的h5地址
        ConfigModel configModel = new ConfigModel();
        configModel.setStore_id(paymentVo.getStoreId());
        configModel = configModelMapper.selectOne(configModel);
        String url = configModel.getH5_domain();

        //获取品牌名称 贝宝客户端id 密钥 brandName clientId clientSecret
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        paymentConfigModel.setPid(15);
        paymentConfigModel.setStore_id(paymentVo.getStoreId());
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });
        if (paymentVo.getStoreType() == STORE_TYPE_PC_MALL)
        {
            url = MapUtils.getString(param, "pc_mall_domain");
        }
        //贝宝支付必须有地址信息，存入地址默认值，防止没地址的支付场景下报错
        param.put("sheng", "sheng");
        param.put("shi", "shi");
        param.put("xian", "xian");
        param.put("address", "address");

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
        //获取h5地址
//        String url = MapUtils.getString(param,"notify_url");

        //获取订单数据  金额  地址
//        OrderModel orderModel = orderModelMapper.selectBySno(paymentVo.getsNo());
        OrderModel orderModel = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());

        param.put("storeId", orderModel.getStore_id());
        param.put("paymentVo", paymentVo);
        BigDecimal zPrice = null;
        if (orderModel.getZ_price() == null)
        {
            zPrice = orderModel.getOld_total();
        }
        else
        {
            zPrice = orderModel.getZ_price();
        }
        param.put("z_price", zPrice);
        String sheng = orderModel.getSheng();

        if (sheng != null && sheng != "")
        {
            param.put("sheng", sheng);
        }
        String shi = orderModel.getShi();
        if (shi != null && shi != "")
        {
            param.put("shi", shi);
        }
        String xian = orderModel.getXian();
        if (xian != null && xian != "")
        {
            param.put("xian", xian);
        }
        String address = orderModel.getAddress();
        if (address != null && address != "")
        {
            param.put("address", address);
        }
        String name = orderModel.getName();
        param.put("name", name);
        //存入sNo 后续直接使用
        param.put("sNo", sNo);
        //存入h5地址
        param.put("url", url);
        //存入real_sno
        param.put("payNo", orderModel.getReal_sno());

        //币种贝宝不支持：CNY 支持：HKD/USD/EUR

        List<String> currencysCode = new ArrayList<>();
        currencysCode.add("HKD");
        currencysCode.add("USD");
        currencysCode.add("EUR");

        boolean supported = currencysCode.contains(orderModel.getCurrency_code());

        if (!supported)
        {
            throw new LaiKeAPIException(ERROR_CODE_PAYPALBZCHB, orderModel.getCurrency_code() + "不支持的币种", "payBack");
        }
        param.put("currency_code", orderModel.getCurrency_code());
        param.put("exchange_rate", orderModel.getExchange_rate());
        param.put("currency_symbol", orderModel.getCurrency_symbol());
        CountryModel countryModel = new CountryModel();
        countryModel.setNum3(user.getCountry_num());
        countryModel = countryModelMapper.selectOne(countryModel);
        String countryCode = "CN";
        if (countryModel != null)
        {
            countryCode = countryModel.getCode();
        }
        param.put("country_code", countryCode);

        try
        {
            String result = createOrder(MODE, param);
            if (StringUtils.isEmpty(result))
            {
                throw new LaiKeAPIException(ERROR_CODE_ZFSB, "支付失败", "pay");
            }
            resultMap.put("data", result);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        return resultMap;
    }

    /**
     * 创建退款请求体
     */
    public RefundRequest buildRefundRequestBody(Map map)
    {
        RefundRequest             refundRequest = new RefundRequest();
        com.paypal.payments.Money money         = new com.paypal.payments.Money();
        money.currencyCode("USD");
        money.value(MapUtils.getString(map, "price"));
        refundRequest.amount(money);
//        refundRequest.invoiceId("T202005230002");
        refundRequest.noteToPayer("申请退款");
        return refundRequest;
    }

    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        //获取品牌名称 贝宝客户端id 密钥 brandName clientId clientSecret
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
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
        String     paypalId   = orderModel.getPaypal_id();
        logger.error("退款使用的贝宝id：{}", paypalId);


//        OrdersGetRequest ordersGetRequest = new OrdersGetRequest(orderId);
        OrdersGetRequest                      ordersGetRequest  = new OrdersGetRequest(paypalId);
        CompsPayPalClient                     payPalClient      = new CompsPayPalClient();
        HttpResponse<com.paypal.orders.Order> ordersGetResponse = null;
        try
        {
            ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
        }
        catch (Exception e)
        {
            try
            {
                log.error("第1次调用paypal订单查询失败");
                ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
            }
            catch (Exception e2)
            {
                try
                {
                    log.error("第2次调用paypal订单查询失败");
                    ordersGetResponse = payPalClient.client(MODE, clientId, clientSecret).execute(ordersGetRequest);
                }
                catch (Exception e3)
                {
                    log.error("第3次调用paypal订单查询失败，失败原因：{}", e3.getMessage());
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
                log.error("第1次调用paypal退款申请失败");
                response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
            }
            catch (Exception e1)
            {
                try
                {
                    log.error("第2次调用paypal退款申请失败");
                    response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
                }
                catch (Exception e2)
                {
                    log.error("第3次调用paypal退款申请失败，失败原因 {}", e2.getMessage());
                }
            }
        }
        log.info("Status Code = {}, Status = {}, RefundID = {}", response.statusCode(), response.result().status(), response.result().id());
        if ("COMPLETED".equals(response.result().status()))
        {
            //进行数据库操作，修改状态为已退款（配合回调和退款查询确定退款成功）
            log.info("退款成功");
        }
        for (com.paypal.payments.LinkDescription link : response.result().links())
        {
            log.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
        }
//        String json = new JSONObject(new Json().serialize(response.result())).toString(4);
//        log.info("refundOrder response body: {}", json);
//        return response;

        return null;
    }

    /**
     * 支付回调
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        Map resultMap = Maps.newHashMap();

        log.error("进入贝宝回调方法！！");
        log.error("map具体内容：" + params.toString());
        log.info("map具体内容：" + params.toString());
//        String sNo             = (String) params.get("custom");
//        String payNo           = DataUtils.getStringVal(params, "custom");
        String payNo = DataUtils.getStringVal(params, "reference_id");
        String paymentStatus   = (String) params.get("payment_status");
        String amount          = (String) params.get("mc_gross");
        String currency        = (String) params.get("mc_currency");
        String paymentId       = (String) params.get("txn_id");
        String parentPaymentId = (String) params.get("parent_txn_id");
        log.info("商家订单号（payNo） = {}", payNo);
        log.info("订单状态 = {}", paymentStatus);
        log.info("金额 = {}", amount);
        log.info("币种 = {}", currency);
        log.info("流水号 = {}", paymentId);
        if (parentPaymentId != null && !parentPaymentId.isEmpty())
        {
            log.info("父流水号 = {}", parentPaymentId);
        }

        //判断是否回调成功
        if (!"Completed".equals(paymentStatus))
        {
            log.info("支付失败,状态不为=COMPLETED");
            resultMap.put("code", ERROR_CODE_BBHDSB);
            resultMap.put("message", "error");
            return resultMap;
        }

        try
        {
            int storeId = 0;
            storeId = Integer.parseInt(DataUtils.getStringVal(params, "address_zip"));
            //贝宝推送标题
            String title = "";
            logger.info("payBack-start-params-{}", JSON.toJSONString(params));
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            logger.info("paymentVo-{}", JSON.toJSONString(paymentVo));
            // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
//            String payNo = DataUtils.getStringVal(params, "custom");
            logger.error("payNo:" + payNo);
//            logger.info("报空total完整map:" + params);
            BigDecimal total = DataUtils.getBigDecimalVal(params, "total");

            if (StringUtils.isEmpty(payNo) || total.compareTo(BigDecimal.ZERO) <= 0)
            {
                logger.error("普通订单回调失败信息 订单：{},支付金额：{}", payNo, total);
                resultMap.put("code", API_OPERATION_FAILED);
                resultMap.put("message", "error");
                return resultMap;
            }
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
            String sNo = orderModelMapper.getOrderByRealSno(payNo);
            logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
            if (StringUtils.isEmpty(sNo))
            {
                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
                logger.info("先拆单后支付:{}", sNo);
            }
            //钱包充值sNo = null
            if (sNo == null)
            {
                sNo = payNo;
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel != null)
            {
                storeId = orderModel.getStore_id();
            }
            logger.info("贝宝版本2回调订单处理");
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackUpOrderMember(orderDataModel);
                title = "充值";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackForMember(orderDataModel);
                title = "办理会员";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                //预售订单信息
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setReal_sno(payNo);
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                //主订单信息
                OrderModel preSellOrder = new OrderModel();
                preSellOrder.setsNo(preSellRecordModel.getsNo());
                preSellOrder = orderModelMapper.selectOne(preSellOrder);
                //商品信息
                PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                //只有支付定金时需要用到
                paymentVo.setPayment_money(total.divide(new BigDecimal(100)).toString());
                if (preSellRecordModel.getPay_type() == null)
                {
                    paymentVo.setPayTarget(3);
                }
                else if (preSellRecordModel.getPay_type().equals(PreSellRecordModel.BALANCE))
                {
                    paymentVo.setPayTarget(2);
                }
                else
                {
                    preSellOrder = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                    paymentVo.setPayTarget(1);
                }
                resultMap = publicOrderService.payBackForPreSell(paymentVo, preSellOrder);
                title = preSellGoodsModel.getProduct_title();
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderMchPromise(orderDataModel);
                title = "店铺保证金";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderAuctionPromise(orderDataModel);
                title = "竞拍保证金";
            }
            else
            {
                //是否有内部api参数,如果有则调用内部实现 add by trick 2022-08-09 17:32:34
                Map<String, Object> paramMap = new HashMap<>(1);
                try
                {
                    paramMap = JSON.parseObject(paymentVo.getParameter(), new TypeReference<Map<String, Object>>()
                    {
                    });
                }
                catch (JSONException ignored)
                {
                    logger.error("贝宝回调parameter参数错误:{}", paymentVo.getParameter());
                }
                String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                if (StringUtils.isNotEmpty(laikeApiUrl))
                {
                    logger.debug("订单{}支付成功回调 执行接口:{}", paymentVo.getsNo(), laikeApiUrl);
                    //region 是否有内部api参数,如果有则调用内部实现(用于支付后下单场景) add by trick 2023-04-24 11:02:06
                    //临时订单转变插件订单-内部接口不要暴露到外网
                    if (paymentVo.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                    {
                        //获取临时表订单数据
                        OrderDataModel orderDataModel = new OrderDataModel();
                        orderDataModel.setPay_type(paymentVo.getPayType());
                        orderDataModel.setTrade_no(paymentVo.getsNo());
                        orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                        if (orderDataModel == null)
                        {
                            throw new LaiKeAPIException(API_OPERATION_FAILED, "贝宝支付失败", "wechatJsapiPay");
                        }
                        //下单
                        OrderVo orderVo = new OrderVo();
                        orderVo.setStoreId(storeId);
                        orderVo.setUserId(paymentVo.getUserId());
                        orderVo.setStoreType(paymentVo.getStoreType());
                        orderVo.setProductsInfo(orderDataModel.getData());
                        //支付订单
                        orderVo.setRealSno(paymentVo.getsNo());
                        orderVo.setPayType(paymentVo.getPayType());
                        Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(orderVo));

                        Map<String, Object> resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.app.payment", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        logger.info("plugin.group.app.payment远程调用返回值: " + JSON.toJSONString(resultMapOrder));
                        paymentVo.setsNo(MapUtils.getString(resultMapOrder, "sNo"));

                        //标记临时订单已支付
                        OrderDataModel orderDataUpdate = new OrderDataModel();
                        orderDataUpdate.setId(orderDataModel.getId());
                        orderDataUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                        orderDataModelMapper.updateByPrimaryKeySelective(orderDataUpdate);
                        logger.error("临时开团订单已更新");
                    }
                    //endregion

                    logger.error("正在支付订单{} 执行接口:{}", payNo, laikeApiUrl);
                    Map<String, Object> paramMap1 = new HashMap<>();
                    logger.error("paymentVo值:{}", JSON.toJSONString(paymentVo));
                    paramMap1.put("vo", JSON.toJSONString(paymentVo));
                    logger.error("paramJson值:{}", paramMap1);
                    resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                    logger.error("支付回调成功 返回数据{}", JSON.toJSONString(resultMap));
                }
                else
                {
                    //普通订单回调
                    if (orderModel != null)
                    {
                        /**更新订单*/
                        //分账代码
                        String transactionId = MapUtils.getString(params, "transactionId");
                        orderModel.setTransaction_id(transactionId);
                        Object fzFlag = redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo);
                        if (fzFlag != null && StringUtils.isNotEmpty(fzFlag.toString()))
                        {
                            if ("Y".equals(redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString()))
                            {
                                logger.info("获取redis缓存数据 返回数据{}", redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString());
                                orderModel.setDividend_status(1);
                            }
                        }
                        else
                        {
                            orderModel.setDividend_status(0);
                        }
                        resultMap = publicOrderService.payBackUpOrder(orderModel);
                        storeId = orderModel.getStore_id();
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setStore_id(storeId);
                        orderDetailsModel.setR_sNo(orderModel.getsNo());
                        List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                        if (select.size() > 0)
                        {
                            OrderDetailsModel orderDetail = select.get(0);
                            title = orderDetail.getP_name();
                            if (select.size() > 1)
                            {
                                title = title + "等商品";
                            }
                        }
                    }
                }
            }
            redisUtil.del("lock:order:" + sNo);

        }
        catch (Exception e)
        {
            logger.error("贝宝回调失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "贝宝回调失败", "payBack");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payBackLocal(MainVo vo, String payNo) throws LaiKeAPIException, UnsupportedEncodingException
    {
        Map resultMap = Maps.newHashMap();



        //贝宝回调控制层逻辑
        String payConfigStr = null;

        //不同类型的订单获取支付信息的方法不一样
        if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ) || payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
        {
            payConfigStr = publicPaymentConfigService.getPaymentConfigByMemberPayNo(payNo);
        }
        else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
        {
            payConfigStr = publicPaymentConfigService.getPaymentConfigByMemberPayNo(payNo);
        }
        else
        {
            logger.error("payNo为:{}", payNo);
//            payConfigStr = publicPaymentConfigService.getPaymentConfigBySNo(sNo);
            payConfigStr = publicPaymentConfigService.getPaymentConfigByPayNo(payNo);
        }
        payConfigStr = URLDecoder.decode(payConfigStr, GloabConst.Chartset.UTF_8);
        logger.info("payConfigStr:{}", payConfigStr);
        JSONObject payConfigJson = JSONObject.parseObject(payConfigStr);
        logger.info("payConfigJson:{}", payConfigJson);

//        params.put("type", payConfigJson.getString("payType"));
        logger.info("type:{}", payConfigJson.getString("payType"));
//        params.put("storeId", payConfigJson.getString("storeId"));
        logger.info("storeId:{}", payConfigJson.getString("storeId"));
        JSONObject wechatPayConfigJson = payConfigJson.getJSONObject("payConfig");
        logger.info("wechatPayConfigJson:{}", payConfigJson.getJSONObject("payConfig"));
        //不存在的情况直接返回错误
        if (!wechatPayConfigJson.containsKey("mch_key"))
        {
            logger.info("贝宝v2本地回调失败1:{}", payConfigStr);
        }
//        params.put("key", wechatPayConfigJson.getString("mch_key"));
        logger.info("key:{}", wechatPayConfigJson.getString("mch_key"));

        PaymentVo paymentVo = new PaymentVo();
        paymentVo.setPayType(payConfigJson.getString("payType"));
        paymentVo.setStoreId(Integer.valueOf(payConfigJson.getString("storeId")));
//        paymentVo.setsNo(payNo);
        paymentVo.setReal_sno(payNo);
        if (payConfigJson.getString("userId") != null)
        {
            paymentVo.setUserId(payConfigJson.getString("userId"));
        }
        if (payConfigJson.getString("map") != null)
        {
            paymentVo.setParameter(payConfigJson.getString("map"));
        }
        if (payConfigJson.getString("real_sno") != null)
        {
            paymentVo.setReal_sno(payConfigJson.getString("real_sno"));
        }
        if (payConfigJson.getString("storeType") != null)
        {
            paymentVo.setStoreType(payConfigJson.getInteger("storeType"));
        }

        log.error("进入贝宝本地回调方法！！");

        log.info("商家订单号（payNo） = {}", payNo);


        try
        {
            int storeId = 0;
            storeId = vo.getStoreId();
            //贝宝推送标题
            String title = "";
            logger.info("paymentVo-{}", JSON.toJSONString(paymentVo));
            // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
            logger.error("payNo:" + payNo);
            BigDecimal total = BigDecimal.valueOf(80);

            if (StringUtils.isEmpty(payNo) || total.compareTo(BigDecimal.ZERO) <= 0)
            {
                logger.error("普通订单回调失败信息 订单：{},支付金额：{}", payNo, total);
                resultMap.put("code", API_OPERATION_FAILED);
                resultMap.put("message", "error");
                return resultMap;
            }
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
            String sNo = orderModelMapper.getOrderByRealSno(payNo);
            logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
            if (StringUtils.isEmpty(sNo))
            {
                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
                logger.info("先拆单后支付:{}", sNo);
            }
            //钱包充值sNo = null
            if (sNo == null)
            {
                sNo = payNo;
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel != null)
            {
                storeId = orderModel.getStore_id();
            }
            logger.info("贝宝版本2回调订单处理");
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackUpOrderMember(orderDataModel);
                title = "充值";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackForMember(orderDataModel);
                title = "办理会员";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                //预售订单信息
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setReal_sno(payNo);
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                //主订单信息
                OrderModel preSellOrder = new OrderModel();
                preSellOrder.setsNo(preSellRecordModel.getsNo());
                preSellOrder = orderModelMapper.selectOne(preSellOrder);
                //商品信息
                PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                //只有支付定金时需要用到
                logger.error("preSellRecordModel.getPay_type()为:{}", preSellRecordModel.getPay_type());
                paymentVo.setPayment_money(total.divide(new BigDecimal(100)).toString());
                if (preSellRecordModel.getPay_type() == null)
                {
                    paymentVo.setPayTarget(3);
                }
                else if (preSellRecordModel.getPay_type().equals(PreSellRecordModel.BALANCE))
                {
                    paymentVo.setPayTarget(2);
                }
                else
                {
                    preSellOrder = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                    paymentVo.setPayTarget(1);
                }
                logger.error("paymentVo为：{}", paymentVo.toString());
                resultMap = publicOrderService.payBackForPreSell(paymentVo, preSellOrder);
                title = preSellGoodsModel.getProduct_title();
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderMchPromise(orderDataModel);
                title = "店铺保证金";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderAuctionPromise(orderDataModel);
                title = "竞拍保证金";
            }
            else
            {
                //是否有内部api参数,如果有则调用内部实现 add by trick 2022-08-09 17:32:34
                Map<String, Object> paramMap = new HashMap<>(1);
                try
                {
                    paramMap = JSON.parseObject(paymentVo.getParameter(), new TypeReference<Map<String, Object>>()
                    {
                    });
                }
                catch (JSONException ignored)
                {
                    logger.error("贝宝本地回调parameter参数错误:{}", paymentVo.getParameter());
                }
                String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                if (StringUtils.isNotEmpty(laikeApiUrl))
                {
                    logger.debug("订单{}支付成功回调 执行接口:{}", paymentVo.getsNo(), laikeApiUrl);
                    //region 是否有内部api参数,如果有则调用内部实现(用于支付后下单场景) add by trick 2023-04-24 11:02:06
                    //临时订单转变插件订单-内部接口不要暴露到外网
                    if (paymentVo.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                    {
                        //获取临时表订单数据
                        OrderDataModel orderDataModel = new OrderDataModel();
                        orderDataModel.setPay_type(paymentVo.getPayType());
                        orderDataModel.setTrade_no(paymentVo.getsNo());
                        orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                        if (orderDataModel == null)
                        {
                            throw new LaiKeAPIException(API_OPERATION_FAILED, "贝宝支付失败", "wechatJsapiPay");
                        }
                        //下单
                        OrderVo orderVo = new OrderVo();
                        orderVo.setStoreId(storeId);
                        orderVo.setUserId(paymentVo.getUserId());
                        orderVo.setStoreType(paymentVo.getStoreType());
                        orderVo.setProductsInfo(orderDataModel.getData());
                        //支付订单
                        orderVo.setRealSno(paymentVo.getsNo());
                        orderVo.setPayType(paymentVo.getPayType());
                        Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(orderVo));

                        Map<String, Object> resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.app.payment", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        logger.info("plugin.group.app.payment远程调用返回值: " + JSON.toJSONString(resultMapOrder));
                        paymentVo.setsNo(MapUtils.getString(resultMapOrder, "sNo"));

                        //标记临时订单已支付
                        OrderDataModel orderDataUpdate = new OrderDataModel();
                        orderDataUpdate.setId(orderDataModel.getId());
                        orderDataUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                        orderDataModelMapper.updateByPrimaryKeySelective(orderDataUpdate);
                        logger.error("临时开团订单已更新");
                    }
                    //endregion

                    logger.error("正在支付订单{} 执行接口:{}", payNo, laikeApiUrl);
                    Map<String, Object> paramMap1 = new HashMap<>();
                    logger.error("paymentVo值:{}", JSON.toJSONString(paymentVo));
                    paramMap1.put("vo", JSON.toJSONString(paymentVo));
                    logger.error("paramJson值:{}", paramMap1);
                    resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                    logger.error("支付回调成功 返回数据{}", JSON.toJSONString(resultMap));
                }
                else
                {
                    //普通订单回调
                    if (orderModel != null)
                    {
                        /**更新订单*/
                        //分账代码
//                        String transactionId = MapUtils.getString(params, "transactionId");
//                        orderModel.setTransaction_id(transactionId);
                        Object fzFlag = redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo);
                        if (fzFlag != null && StringUtils.isNotEmpty(fzFlag.toString()))
                        {
                            if ("Y".equals(redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString()))
                            {
                                logger.info("获取redis缓存数据 返回数据{}", redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString());
                                orderModel.setDividend_status(1);
                            }
                        }
                        else
                        {
                            orderModel.setDividend_status(0);
                        }
                        resultMap = publicOrderService.payBackUpOrder(orderModel);
                        storeId = orderModel.getStore_id();
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setStore_id(storeId);
                        orderDetailsModel.setR_sNo(orderModel.getsNo());
                        List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                        if (select.size() > 0)
                        {
                            OrderDetailsModel orderDetail = select.get(0);
                            title = orderDetail.getP_name();
                            if (select.size() > 1)
                            {
                                title = title + "等商品";
                            }
                        }
                    }
                }
            }


        }
        catch (Exception e)
        {
            logger.error("贝宝本地回调失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "贝宝本地回调失败", "payBackLocal");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 生成订单主体信息
     *
     * @return
     */
    @Override
    public OrderRequest buildRequestBody(Map params)
    {
        PaymentVo    paymentVo    = (PaymentVo) params.get("paymentVo");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent(CAPTURE);//必填

        String suffix = new String();
        String payNo  = params.get("payNo").toString();
        logger.error("生成订单时的支付订单号为：{}", payNo);
        //根据订单类型决定支付成功后的跳转路径
        int storeType = paymentVo.getStoreType();

        if (storeType != STORE_TYPE_PC_MALL)
        {
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                //拼团支付成功跳转路径
//            suffix = "/pagesA/group/group_end";
                suffix = "/pagesA/group/groupDetailed";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                //店铺保证金支付成功跳转路径
                suffix = "/pagesC/bond/success";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                //钱包充值支付成功跳转路径
                suffix = "/pagesB/myWallet/rechargeSuccess?mylei=1";
            }
            else
            {
                suffix = "/pages/pay/PayResults";
            }
        }
        else
        {
            //pc端普通订单
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
            {
                //默认跳转路径
                suffix = "/pay/scanCode";
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                //零钱充值
                suffix = "/my/my/recharge";
            }
        }


        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(MapUtils.getString(params, "brand_name"))
                .landingPage(LANDINGPAGE)
                .cancelUrl(MapUtils.getString(params, "url"))
                .returnUrl(MapUtils.getString(params, "url") + suffix)  //线上使用
//                .cancelUrl(MapUtils.getString(params, "url")).returnUrl("http://192.168.1.127:8081/#" + suffix) //本地调试用
                .userAction(USERACTION)
                .shippingPreference(SHIPPINGPREFERENCE);
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<PurchaseUnitRequest>();

        //todo 货币编码 currencyCode需要在paypal控制台设置支持哪些币种
        String currencyCode = MapUtils.getString(params, "currency_code", "USD");
        //国家编码
        String     countryCode  = MapUtils.getString(params, "country_code");
        BigDecimal exchangeRate = new BigDecimal(MapUtils.getDouble(params, "exchange_rate", 1.0));
        BigDecimal zPrice       = new BigDecimal(MapUtils.getDouble(params, "z_price"));

        //最终支付价格
        zPrice = zPrice.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
//                .referenceId(params.get("storeId").toString())
                .referenceId(params.get("payNo").toString())
//                .description("新一代读写一体，智能电子笔记本")
//                .customId(params.get("payNo").toString())   //填payNo 也就是real_sNo
                .customId(params.get("storeId").toString())   //填payNo 也就是real_sNo
//                .invoiceId("SNOY222212345678")
                .amountWithBreakdown(new AmountWithBreakdown()  //必填
                        .currencyCode(currencyCode)
                        .value(zPrice.toString())// value = itemTotal + shipping + handling + taxTotal + shippingDiscount;
                        .amountBreakdown(new AmountBreakdown()
                                .itemTotal(new Money().currencyCode(currencyCode).value(zPrice.toString())) // itemTotal = Item[Supernote A6](value × quantity) + Item[帆布封套](value × quantity)
                                .shipping(new Money().currencyCode(currencyCode).value("0.00"))
                                .handling(new Money().currencyCode(currencyCode).value("0.00"))
                                .taxTotal(new Money().currencyCode(currencyCode).value("0.00"))
                                .shippingDiscount(new Money().currencyCode(currencyCode).value("0.00"))))
                .shippingDetail(new ShippingDetail()    //必填
                        .name(new Name().fullName((String) params.get("name")))
                        .addressPortable(new AddressPortable()
                                .addressLine1((String) params.get("address"))
                                .addressLine2((String) params.get("xian"))
                                .adminArea2((String) params.get("shi"))
                                .adminArea1((String) params.get("sheng"))
//                                .postalCode("20000")
                                .postalCode(params.get("storeId").toString())
                                .countryCode(countryCode)));
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

    /**
     * 创建订单的方法
     *
     * @throws
     */
    @Override
    public String createOrder(String mode, Map params) throws LaiKeAPIException
    {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(buildRequestBody(params));
        CompsPayPalClient   payPalClient = new CompsPayPalClient();
        HttpResponse<Order> response     = null;

        String clientId     = MapUtils.getString(params, "client_id");
        String clientSecret = MapUtils.getString(params, "client_secret");

        try
        {
            response = payPalClient.client(mode, clientId, clientSecret).execute(request);
        }
        catch (IOException e1)
        {
            try
            {
                log.error("第1次调用paypal订单创建失败");
                response = payPalClient.client(mode, clientId, clientSecret).execute(request);
            }
            catch (Exception e)
            {
                try
                {
                    log.error("第2次调用paypal订单创建失败");
                    response = payPalClient.client(mode, clientId, clientSecret).execute(request);
                }
                catch (Exception e2)
                {
                    log.error("第3次调用paypal订单创建失败，失败原因：{}", e2.getMessage());
                    throw new LaiKeAPIException(ERROR_CODE_ZFSB, "支付失败", "createOrder");
                }
            }
        }

        String approve = "";
        if (response != null)
        {
            if (response.statusCode() == 201)
            {
                log.info("Status Code = {}, Status = {}, OrderID = {}, Intent = {}", response.statusCode(), response.result().status(), response.result().id(), response.result().checkoutPaymentIntent());
                for (LinkDescription link : response.result().links())
                {
                    log.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
                    if (link.rel().equals("approve"))
                    {
                        approve = link.href();
                    }
                }
                String totalAmount = response.result().purchaseUnits().get(0).amountWithBreakdown().currencyCode() + ":" + response.result().purchaseUnits().get(0).amountWithBreakdown().value();
                log.info("Total Amount: {}", totalAmount);
                Object result = response.result();
                String json   = JSON.toJSONString(result);
                log.info("createOrder response body: {}", json);
            }
        }
        return approve;
    }



    /**
     * 用户授权支付成功，进行扣款操作
     */
    public Map<String, Object> captureOrder(MainVo vo, String orderId, String sNo) throws IOException
    {
        int  storeId = vo.getStoreId();
        //这里getLktUser 兼容pc商城和移动端买家和卖家的user信息获取
        User user = RedisDataTool.getLktUser(vo.getAccessId(), redisUtil);
        logger.error("user为：" + user.toString());


        //存入订单的贝宝订单号
        int i = orderModelMapper.updatePaypalIdBySno(sNo, orderId);
        logger.info("进入捕获订单接口，参数sNo为：" + sNo + "，orderId为：" + orderId);
        logger.info("在lkt_order修改了paypal_id " + i + " 条");

        //给前端的返回值
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pay_type", "paypal");

        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.requestBody(new OrderRequest());
        CompsPayPalClient   payPalClient = new CompsPayPalClient();
        HttpResponse<Order> response     = null;
        //获取品牌名称 贝宝客户端id 密钥 brandName clientId clientSecret
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        paymentConfigModel.setPid(15);
        paymentConfigModel.setStore_id(storeId);
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


        try
        {
            response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
        }
        catch (IOException e1)
        {
            try
            {
                log.error("第1次调用paypal扣款失败");
                response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
            }
            catch (Exception e)
            {
                try
                {
                    log.error("第2次调用paypal扣款失败");
                    response = payPalClient.client(MODE, clientId, clientSecret).execute(request);
                }
                catch (Exception e2)
                {
                    log.error("第3次调用paypal扣款失败，失败原因 {}", e2.getMessage());
                }
            }
        }
        log.info("Status Code = {}, Status = {}, OrderID = {}", response.statusCode(), response.result().status(), response.result().id());
        for (LinkDescription link : response.result().links())
        {
            log.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
        }
        String referenceId = new String();
        for (PurchaseUnit purchaseUnit : response.result().purchaseUnits())
        {
            referenceId = purchaseUnit.referenceId(); //获取payNo
            logger.error("orderId为：{}", orderId);
            logger.error("referenceId为：{}", referenceId);

            for (Capture capture : purchaseUnit.payments().captures())
            {
                log.info("Capture id: {}", capture.id());
                log.info("status: {}", capture.status());
                log.info("invoice_id: {}", capture.invoiceId());
                if ("COMPLETED".equals(capture.status()))
                {
                    //进行数据库操作，修改订单状态为已支付成功，尽快发货（配合回调和CapturesGet查询确定成功）
                    log.info("支付成功,状态为=COMPLETED");
                    System.out.println("扣款成功！！");
                }
                if ("PENDING".equals(capture.status()))
                {
                    log.info("status_details: {}", capture.captureStatusDetails().reason());
                    String reason = "PENDING";
                    if (capture.captureStatusDetails() != null && capture.captureStatusDetails().reason() != null)
                    {
                        reason = capture.captureStatusDetails().reason();
                    }
                    //进行数据库操作，修改订单状态为已支付成功，但触发了人工审核，请审核通过后再发货（配合回调和CapturesGet查询确定成功）
                    log.info("支付成功,状态为=PENDING : {}", reason);
                }
            }
        }
        Payer buyer = response.result().payer();
        log.info("Buyer Email Address: {}", buyer.email());
        log.info("Buyer Name: {} {}", buyer.name().givenName(), buyer.name().surname());

        String json   = JSON.toJSONString(response.result());
        log.error("！！！！！！captureOrder response body: {}", json);

        // 使用 FastJSON 将 response 对象转换为 JSON 字符串
        Order  order        = response.result(); // 获取真正的业务数据
        String jsonResponse = JSON.toJSONString(order); // 序列化 Order 对象
        resultMap.put("data", jsonResponse);


        logger.info("订单号为：" + sNo);
        //店铺保证金操作
        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
        {
            logger.info("进保证金判断了");
            user.setUser_id(user.getUser_id());
            user = userMapper.selectOne(user);
            //获取用户店铺
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);

            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(1, null);
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCDPPZWCSH, "商城店铺配置未初始化");
            }
            if (mchConfigModel.getPromise_switch() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPWKQBZJ, "店铺未开启保证金");
            }

            MchPromiseModel mchPromiseSave = new MchPromiseModel();
            OrderDataModel  orderDataSave  = new OrderDataModel();

            //下单操作
            OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);

            int row = 0;
            if (orderDataByTradeNo == null)
            {
                Map<String, Object> dataMap = new HashMap<>(16);
                dataMap.put("paymentAmt", mchConfigModel.getPromise_amt());
                dataMap.put("storeId", mchModel.getStore_id());
                dataMap.put("mchId", mchModel.getId());
                dataMap.put("pay", "paypal");
                dataMap.put("user_id", user.getUser_id());
                dataMap.put("paypal_id", orderId);
                orderDataSave.setData(JSON.toJSONString(dataMap));
                orderDataSave.setTrade_no(sNo);
                orderDataSave.setOrder_type("PR");
                orderDataSave.setAddtime(new Date());
                row = orderDataModelMapper.insertSelective(orderDataSave);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }
            else
            {
                logger.info("lkt_order_data表进行修改操作");
                Map<String, Object> dataMap = new HashMap<>(16);
                dataMap.put("paymentAmt", mchConfigModel.getPromise_amt());
                dataMap.put("storeId", mchModel.getStore_id());
                dataMap.put("mchId", mchModel.getId());
                dataMap.put("pay", "paypal");
                dataMap.put("user_id", user.getUser_id());
                dataMap.put("paypal_id", orderId);
                orderDataSave.setData(JSON.toJSONString(dataMap));
                orderDataSave.setTrade_no(sNo);
                orderDataSave.setOrder_type("PR");
                row = orderDataModelMapper.updataByTradeNo(orderDataSave);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }

            if (StringUtils.isNotEmpty(sNo))
            {
                //是否有生成过订单,如果生成过则修改记录
                MchPromiseModel mchPromiseOld = new MchPromiseModel();
                mchPromiseOld.setMch_id(mchModel.getId());
                mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);

                //支付记录
                mchPromiseSave.setPay_type("paypal");
                mchPromiseSave.setOrderNo(sNo);
                mchPromiseSave.setPromise_amt(mchConfigModel.getPromise_amt());
                mchPromiseSave.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                if (mchPromiseOld == null)
                {
                    //支付成功
                    mchPromiseSave.setAdd_date(new Date());
                    mchPromiseSave.setMch_id(mchModel.getId());
                    mchPromiseSave.setId(BuilderIDTool.getGuid());
                    row = mchPromiseModelMapper.insertSelective(mchPromiseSave);
                }
                else
                {
                    mchPromiseSave.setId(mchPromiseOld.getId());
                    mchPromiseSave.setIs_return_pay(DictionaryConst.WhetherMaven.WHETHER_NO);
                    //更新缴纳时间
                    mchPromiseSave.setAdd_date(new Date());
                    //修改时间
                    mchPromiseSave.setUpdate_date(new Date());
                    row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseSave);
                }

                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
                }

                //添加缴纳店铺保证金记录
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(user.getStore_id());
                recordModel.setUser_id(user.getUser_id());
                recordModel.setMoney(mchConfigModel.getPromise_amt());
                recordModel.setOldmoney(user.getMoney());
                recordModel.setEvent("缴纳店铺保证金");
                recordModel.setType(RecordModel.RecordType.PAY_MCH_BOND);
                recordModel.setAdd_date(new Date());
                recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                recordModel.setMain_id(mchModel.getId().toString());
                recordModelMapper.insertSelective(recordModel);
                logger.error("店铺缴纳保证金记录代码已执行");

                PromiseRecordModel promiseRecordModel = new PromiseRecordModel();
                promiseRecordModel.setStore_id(user.getStore_id());
                promiseRecordModel.setMch_id(mchModel.getId());
                promiseRecordModel.setMoney(mchConfigModel.getPromise_amt());
                promiseRecordModel.setType(PromiseRecordModel.RecordType.PAY_MCH_MARGIN);
                promiseRecordModel.setAdd_date(new Date());
                promiseRecordModelMapper.insertSelective(promiseRecordModel);
            }

        }

        //预售订单操作
        else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
        {
            //如果定金不存在贝宝id，先给定金存入贝宝id
            if (null == preSellRecordModelMapper.selectPaypalIdBySNoAndPayType(sNo, 0))
            {
                int ps = preSellRecordModelMapper.updatePaypalIdBySNoAndPayType(sNo, orderId, 0);
                logger.info("预售定金订单存入了 " + ps + " 个贝宝id");
            }
            else
            {
                //定金已存在贝宝id了，给尾款存贝宝id
                int ps = preSellRecordModelMapper.updatePaypalIdBySNoAndPayType(sNo, orderId, 1);
                logger.info("预售尾款订单存入了 " + ps + " 个贝宝id");
            }
        }

        //竞拍保证金操作
        else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
        {
            //将贝宝id存到lkt_order_data表的json字符串data中
            OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
            String         data               = orderDataByTradeNo.getData();
            JSONObject     dataJson           = JSONObject.parseObject(data);
            dataJson.put("paypal_id", orderId);
            orderDataByTradeNo.setData(dataJson.toJSONString());
            orderDataModelMapper.updataByTradeNo(orderDataByTradeNo);
            logger.error("竞拍保证金捕获订单后的orderdata数据：" + orderDataModelMapper.getOrderDataByTradeNo(sNo).toString());
        }

        //拼团订单操作
        else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
        {
            //将贝宝id存到lkt_order_data表的json字符串data中
            OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
            String         data               = orderDataByTradeNo.getData();
            JSONObject     dataJson           = JSONObject.parseObject(data);
            dataJson.put("paypal_id", orderId);
            orderDataByTradeNo.setData(dataJson.toJSONString());
            orderDataModelMapper.updataByTradeNo(orderDataByTradeNo);
            logger.error("拼团捕获订单后的orderdata数据：" + orderDataModelMapper.getOrderDataByTradeNo(sNo).toString());
        }


        if (!sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
        {
            payBackLocal(vo, referenceId);
        }
        resultMap.put("payNo", referenceId);

        return resultMap;
    }


    /**
     * 贝宝提现
     *
     * @param params
     * @return
     * @throws Exception
     */
    public String payOut(String params) throws Exception
    {
        JSONObject jsonObject = JSON.parseObject(params);

        //获取品牌名称 贝宝客户端id 密钥 brandName clientId clientSecret
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        paymentConfigModel.setPid(15);
        paymentConfigModel.setStore_id(Integer.valueOf(MapUtils.getString(jsonObject, "storeId")));
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });
        String clientId     = MapUtils.getString(param, "client_id");
        String clientSecret = MapUtils.getString(param, "client_secret");

        //获取AccessToken
        String accessTokenRes = getAccessToken(clientId, clientSecret);//拿到总字符串

        JSONObject jsonObject1 = JSONObject.parseObject(accessTokenRes);
        String     accessToken = jsonObject1.getString("access_token");//拿到access_token

        //设置请求体
        String prefix = jsonObject.getString("prefix");
        int        withdrawId1 = (int) jsonObject.get("withdrawId");
//        String     withdrawId  = String.valueOf(withdrawId1);        //提现id
        String withdrawId = generateUniqueId();        //提现id
        BigDecimal money1      = (BigDecimal) jsonObject.get("money");//提现金额
        String     money       = String.valueOf(money1);
        String     email       = (String) jsonObject.get("email");                //用户贝宝邮箱

        String requestBodyTemplate = "{ \"sender_batch_header\": { \"sender_batch_id\": \"%s\", \"email_subject\": \"You have a payout!\", \"email_message\": \"You have received a payout! Thanks for using our service!\" }, \"items\": [ { \"recipient_type\": \"EMAIL\", \"amount\": { \"value\": \"%s\", \"currency\": \"USD\" }, \"note\": \"Thanks for your patronage!\", \"sender_item_id\": \"20231001001\", \"receiver\": \"%s\" } ] }";
        String requestBody         = String.format(requestBodyTemplate, withdrawId, money, email);
        logger.error("贝宝提现requestBody为：{}", requestBody);



        //发送提现请求
        String res = sendPayout(accessToken, requestBody);
        logger.error("res响应为：" + res);

        return res;
    }

    /**
     * 生成唯一的 sender_batch_id
     *
     * @return
     */
    public static String generateUniqueId()
    {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 创建一个 SecureRandom 对象
        java.security.SecureRandom random = new java.security.SecureRandom();
        // 生成一个 4 位的随机数
        int randomNumber = random.nextInt(9000) + 1000;
        // 将时间戳和随机数拼接成唯一 ID
        return String.valueOf(timestamp) + randomNumber;
    }

    /**
     * 获取提现要用的AccessToken
     *
     * @param clientId
     * @param clientSecret
     * @return
     * @throws Exception
     */
    public static String getAccessToken(String clientId, String clientSecret) throws LaiKeAPIException, IOException
    {
        URL               url      = new URL("https://api-m.sandbox.paypal.com/v1/oauth2/token");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        // 设置请求头
        String auth        = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        httpConn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // 设置请求体
        httpConn.setDoOutput(true);
        try (OutputStream os = httpConn.getOutputStream())
        {
            os.write("grant_type=client_credentials".getBytes());
        }

        // 获取响应
        try (Scanner s = new Scanner(httpConn.getInputStream()).useDelimiter("\\A"))
        {
            String response = s.hasNext() ? s.next() : "";
            return response; // 返回包含 access_token 的 JSON 响应
        }
    }

    public static void main(String[] args) throws Exception
    {
        String clientId     = "ATLR3XC3_Z_URp8pWKxAUuPY5u-wRU1bPsysYMDCE3nTX8dhzdQ9XEsn0Ay3sEuQmtUxQM_B9H5UGRKO";
        String clientSecret = "EOaf6jaIIoVdrvEDSNCqd52mLmMTaTiFbRDEw9cPzBQX2FWmDEHnnCIg_JuMmVADjQaF6HLcYdTtkcfQ";
        String tokenResponse = getAccessToken(clientId, clientSecret);
        System.out.println(tokenResponse);
        JSONObject jsonObject  = JSONObject.parseObject(tokenResponse);
        String     accessToken = jsonObject.getString("access_token");
        System.out.println(accessToken);

    }

    public static String sendPayout(String accessToken, String requestBody) throws Exception
    {
        URL               url      = new URL("https://api-m.sandbox.paypal.com/v1/payments/payouts");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        // 设置请求头
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);

        // 启用输出流
        httpConn.setDoOutput(true);

        // 写入请求体
        try (OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream()))
        {
            writer.write(requestBody);
        }

        // 获取响应
        try (Scanner s = new Scanner(httpConn.getInputStream()).useDelimiter("\\A"))
        {
            return s.hasNext() ? s.next() : "";
        }
    }

//    public static void main(String[] args) throws Exception {
//        String accessToken = "A21AAJ0bmSkgjHMoFHfmRJVu8QJkUhQilvy5RicQTvWB4UiJ6S49aDKzhXbTTl4w-PwkdZAIYUAFxjUHh_oo5kDskWlnubKXQ";
//        String requestBody = "{ \"sender_batch_header\": { \"sender_batch_id\": \"Payouts_2023_1001\", \"email_subject\": \"You have a payout!\", \"email_message\": \"You have received a payout! Thanks for using our service!\" }, \"items\": [ { \"recipient_type\": \"EMAIL\", \"amount\": { \"value\": \"10.00\", \"currency\": \"USD\" }, \"note\": \"Thanks for your patronage!\", \"sender_item_id\": \"20231001001\", \"receiver\": \"sb-0vpuw36876132@personal.example.com\" } ] }";
//        String payoutResponse = sendPayout(accessToken, requestBody);
//        System.out.println(payoutResponse);
//    }

}
