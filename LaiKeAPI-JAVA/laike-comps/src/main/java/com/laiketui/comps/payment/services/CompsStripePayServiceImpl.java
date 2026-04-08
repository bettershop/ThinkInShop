package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.payment.CompsStripePayService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
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
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.BindStripeEmailVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.pay.StripeWithdrawVo;
import com.laiketui.root.common.BuilderIDTool;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Transfer;
import com.stripe.model.checkout.Session;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.TransferCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.laiketui.comps.payment.util.CompsPayPalCheckoutConstant.SUCCESS;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;
import static com.laiketui.core.lktconst.GloabConst.StoreType.STORE_TYPE_PC_MALL;

@Slf4j
@Service("compsStripePayService")
@RefreshScope
public class CompsStripePayServiceImpl extends CompsPayServiceAdapter implements CompsStripePayService
{
    private final Logger logger = LoggerFactory.getLogger(CompsStripePayServiceImpl.class);

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


    //region 支付请求
    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        PaymentVo           paymentVo = (PaymentVo) params.get("paymentVo");
        User                user      = RedisDataTool.getLktUser(paymentVo.getAccessId(), redisUtil);
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

        //获取支付配置信息
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        paymentConfigModel.setPid(16);
        paymentConfigModel.setStore_id(paymentVo.getStoreId());
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });

        //返回公钥给前端
        String publishableKey = MapUtils.getString(param, "publishable_key");
        if (!com.laiketui.core.utils.tool.StringUtils.isEmpty(publishableKey))
        {
            resultMap.put("publishable_key", publishableKey.replaceAll("%2B", "\\+"));
        }
        else
        {
            logger.error("Stripe公钥未配置");
        }

        if (paymentVo.getStoreType() == STORE_TYPE_PC_MALL)
        {
            url = MapUtils.getString(param, "pc_mall_domain");
        }

        //创建支付会话参数传入stripe密钥
        String secretKey = MapUtils.getString(param, "secret_key");
        if (!StringUtils.isEmpty(secretKey))
        {
            param.put("secret_key", secretKey.replaceAll("%2B", "\\+"));
        }
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

//        if (sheng != null && sheng != "")
//        {
//            param.put("sheng", sheng);
//        }
//        String shi = orderModel.getShi();
//        if (shi != null && shi != "")
//        {
//            param.put("shi", shi);
//        }
//        String xian = orderModel.getXian();
//        if (xian != null && xian != "")
//        {
//            param.put("xian", xian);
//        }
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

        //测试时手动修改币种  非测试需注释
//        orderModel.setCurrency_code("USD");
        //如果币种为空，默认设置为USD
        if (orderModel.getCurrency_code() == null)
        {
            orderModel.setCurrency_code("USD");
        }

        boolean supported = currencysCode.contains(orderModel.getCurrency_code());

        //订单类型不为预售订单、拼团订单、店铺保证金、竞拍保证金，才需要校验币种
        boolean checkCurrencyCode = !sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS) &&
                !sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) &&
                !sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT) &&
                !sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE) &&
                !sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB);

        if (!supported && checkCurrencyCode)
        {
            logger.error("不支持的币种: {}", orderModel.getCurrency_code());
            throw new LaiKeAPIException(ERROR_CODE_STRIPEBZCHB, orderModel.getCurrency_code() + "不支持的币种", "payBack");
        }
        param.put("currency_code", orderModel.getCurrency_code());
        param.put("exchange_rate", orderModel.getExchange_rate());
        param.put("currency_symbol", orderModel.getCurrency_symbol());
        CountryModel countryModel = new CountryModel();
        countryModel.setNum3(user.getCountry_num());
        countryModel = countryModelMapper.selectOne(countryModel);
        String countryCode = "US";
        if (countryModel != null)
        {
            countryCode = countryModel.getCode();
        }
        param.put("country_code", countryCode);

        try
        {
            String stripeId = createPaymentSession(param);
            logger.error("创建Stripe支付会话成功，stripeId: {}", stripeId);
            if (StringUtils.isEmpty(stripeId))
            {
                throw new LaiKeAPIException(ERROR_CODE_ZFSB, "支付失败", "pay");
            }

            //region 根据不同的订单类型，存入stripe会话ID到不同的表中

            //存入订单的stripe支付号
            int i = orderModelMapper.updateStripeIdBySno(sNo, stripeId);
            logger.info("进入捕获订单接口，参数sNo为：" + sNo + "，stripeId为：" + stripeId);
            logger.info("在lkt_order修改了stripeId " + i + " 条");
            //店铺保证金操作
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                logger.info("进保证金判断了");
                user.setUser_id(user.getUser_id());
                user = userMapper.selectOne(user);
                //获取用户店铺
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(orderModel.getStore_id());
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
                    dataMap.put("pay", "stripe");
                    dataMap.put("user_id", user.getUser_id());
                    dataMap.put("stripe_id", stripeId);
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
                    dataMap.put("pay", "stripe");
                    dataMap.put("user_id", user.getUser_id());
                    dataMap.put("stripe_id", stripeId);
                    orderDataSave.setData(JSON.toJSONString(dataMap));
                    orderDataSave.setTrade_no(sNo);
                    orderDataSave.setOrder_type("PR");
                    row = orderDataModelMapper.updataByTradeNo(orderDataSave);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                    }
                }

                if (com.laiketui.core.utils.tool.StringUtils.isNotEmpty(sNo))
                {
                    //是否有生成过订单,如果生成过则修改记录
                    MchPromiseModel mchPromiseOld = new MchPromiseModel();
                    mchPromiseOld.setMch_id(mchModel.getId());
                    mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);

                    //支付记录
                    mchPromiseSave.setPay_type("stripe");
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

                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setsNo(sNo);
                preSellRecordModel.setPay_type(0);
                PreSellRecordModel preSellRecordModel1 = preSellRecordModelMapper.selectOne(preSellRecordModel);
                //如果定金不存在stripe id，先给定金存入stripe id
                if (null == preSellRecordModel1.getStripe_id())
                {
                    preSellRecordModel1.setStripe_id(stripeId);
                    int ps = preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel1);
                    logger.info("预售定金订单存入了 " + ps + " 个stripe id");
                }
                else
                {
                    //定金已存在stripe id了，给尾款存stripe id
                    int ps = preSellRecordModelMapper.updateStripeIdBySNoAndPayType(sNo, stripeId, 1);
                    logger.info("预售尾款订单存入了 " + ps + " 个stripe id");
                }
            }

            //竞拍保证金操作
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                //将贝宝id存到lkt_order_data表的json字符串data中
                OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
                String         data               = orderDataByTradeNo.getData();
                JSONObject     dataJson           = JSONObject.parseObject(data);
                dataJson.put("stripe_id", stripeId);
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
                dataJson.put("stripe_id", stripeId);
                orderDataByTradeNo.setData(dataJson.toJSONString());
                orderDataModelMapper.updataByTradeNo(orderDataByTradeNo);
                logger.error("拼团捕获订单后的orderdata数据：" + orderDataModelMapper.getOrderDataByTradeNo(sNo).toString());
            }
            //endregion

            resultMap.put("stripe_id", stripeId);

        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        return resultMap;
    }


    /**
     * 创建Stripe支付会话
     *
     * @param params 支付参数Map
     * @return 支付会话ID
     * @throws LaiKeAPIException 支付异常
     */
    private String createPaymentSession(Map<String, Object> params) throws LaiKeAPIException
    {
        try
        {
            logger.error("开始创建Stripe支付会话，参数：{}", params);

            // 从参数中获取必要信息
            String sNo = (String) params.get("sNo");
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            String    secretKey = (String) params.get("secret_key");
            String    url       = (String) params.get("url");
            //todo 货币编码 currencyCode需要在paypal控制台设置支持哪些币种
            String currencyCode = MapUtils.getString(params, "currency_code", "USD");

            //国家编码
            String     countryCode  = MapUtils.getString(params, "country_code");
            BigDecimal exchangeRate = new BigDecimal(MapUtils.getDouble(params, "exchange_rate", 1.0));
            BigDecimal zPrice       = new BigDecimal(MapUtils.getDouble(params, "z_price"));

            //最终支付价格
            zPrice = zPrice.multiply(exchangeRate);
            if (zPrice.compareTo(BigDecimal.valueOf(0.51)) < 0)
            {
                // stripe最低支付金额为0.5美元
                throw new LaiKeAPIException(ERROR_CODE_DYZDZFJE, "低于最低支付金额");
            }

            // 设置Stripe API密钥
            Stripe.apiKey = secretKey;

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
                    suffix = "/pagesA/group/group_end";
//                    suffix = "/pagesA/group/groupDetailed";
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
                    suffix = "/my/my/myOrder";
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
                {
                    //零钱充值
                    suffix = "/my/my/recharge";
                }
            }

            // 计算支付金额（Stripe要求以最小货币单位传入，如美分）
            BigDecimal amount = zPrice;
            long amountCents = amount.multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP).longValue();

            // 构建商品描述 此处使用商品名称
//            String productDescription = paymentVo.getTitle();
            // 构建商品描述 此处使用sNo
            String productDescription = sNo;


            // 构建支付成功和取消后的回调URL
//            String successUrl = url + "/pages/pay/success?session_id={CHECKOUT_SESSION_ID}";
            String successUrl = url + suffix;
            String cancelUrl  = url;

            //设置orderlist为空
            paymentVo.setOrder_list("null");

            // 创建Stripe支付会话参数
            SessionCreateParams createParams = SessionCreateParams.builder()
                    // 支付成功后，用户将被重定向到的URL
                    // {CHECKOUT_SESSION_ID} 是Stripe自动插入的会话ID变量，用于后续验证
                    .setSuccessUrl(successUrl)

                    // 支付取消后，用户将被重定向到的URL
                    .setCancelUrl(cancelUrl)

                    // 设置支付模式为"一次性支付"
                    // 可选值：PAYMENT（一次性支付）、SUBSCRIPTION（订阅）、SETUP（仅设置支付方式）
                    .setMode(SessionCreateParams.Mode.PAYMENT)

                    // 添加商品行项目，每个项目代表一个要购买的商品或服务
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    // 设置货币代码（如"usd", "eur", "cny"等）
                                    .setCurrency(currencyCode)

                                    // 设置商品价格，以最小货币单位表示（如美分、日元等）
                                    // 例如：100美分 = 1美元，100日元 = 100日元
                                    .setUnitAmount(amountCents)

                                    // 设置商品或服务的描述信息
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(productDescription)
                                            .build())
                                    .build())
                            // 设置购买数量
                            .setQuantity(1L)
                            .build())

                    // 添加自定义元数据，这些数据会随支付一起保存，可用于后续查询和处理
                    // 通常用于存储与业务相关的ID，如订单ID、店铺ID等
                    .putMetadata("sNo", params.get("sNo").toString())
                    .putMetadata("real_sNo", params.get("payNo").toString())
                    .putMetadata("store_id", params.get("storeId").toString())
//                    .putMetadata("paymentVo", JSON.toJSONString(paymentVo))
                    // 其他可选参数示例：
                    // .setCustomerEmail("customer@example.com") // 设置客户邮箱
                    // .setShippingAddressCollection(...) // 收集配送地址
                    // .setPaymentMethodTypes(Arrays.asList("card")) // 指定支付方式

                    .build();

            // 创建支付会话
            Session session = Session.create(createParams);

            return session.getId();
        }
        catch (LaiKeAPIException e)
        {
            // 直接抛出业务异常
            throw e;
        }
        catch (Exception e)
        {
            logger.error("创建Stripe支付会话失败: {}", e.getMessage(), e);
            throw new LaiKeAPIException(ERROR_CODE_ZFSB, "创建支付会话失败: " + e.getMessage(), "createPaymentSession");
        }
    }

    /**
     * 测试创建Stripe支付会话
     */
    public void testCreatePaymentSession()
    {
        try
        {
            // 创建测试参数
            Map<String, Object> params = new HashMap<>();

            // 创建测试订单
            OrderModel orderModel = new OrderModel();
            orderModel.setReal_sno("TEST_ORDER_123");
            orderModel.setStore_id(1);
            orderModel.setCurrency_code("USD"); // 货币代码
            orderModel.setZ_price(new BigDecimal("8")); // 订单金额
            orderModel.setsNo("Test Product"); // 商品描述

            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType("stripe");
            paymentVo.setTitle("凤梨酥");

//            params.put("orderModel", orderModel);
            params.put("paymentVo", paymentVo);
            params.put("secret_key", "sk_test_51RgKgaRoDsISw3pRFUL4bMy8p2QLe0pEZIDRhOeOAaPjodHucynCPDoeV3g35cJKF8FLMLwIMa3APuxvLCjZu9Je00b2Zf739s");
            params.put("url", "https://your-test-domain.com");
            params.put("z_price", new BigDecimal("8"));
            params.put("payNo", "payNo123");
            params.put("storeId", 1);
            params.put("sNo", "TEST_sNo_123");

            // 调用支付会话创建方法
            String sessionId = createPaymentSession(params);

            // 输出测试结果
            System.out.println("测试成功！创建的支付会话ID: " + sessionId);
            System.out.println("请访问以下URL测试支付流程:");
            System.out.println("https://checkout.stripe.com/pay/" + sessionId);

        }
        catch (LaiKeAPIException e)
        {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.err.println("发生未知错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主方法：用于测试Stripe支付会话创建
     */
//    public static void main(String[] args)
//    {
//        try
//        {
//            // 创建当前服务类的实例
//            CompsStripePayServiceImpl service = new CompsStripePayServiceImpl();
//
//            // 调用测试方法
//            service.testCreatePaymentSession();
//        }
//        catch (Exception e)
//        {
//            System.err.println("测试过程中发生异常: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    //endregion


    //region 回调请求

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
        // 从params中获取accessId（键名需与存入时一致，例如"accessId"）
        String accessId = (String) params.get("accessId");
        // 兼容pc商城和移动端买家和卖家的user信息获取
        User user = RedisDataTool.getLktUser(accessId, redisUtil);

        Map resultMap = com.google.common.collect.Maps.newHashMap();

        log.error("进入stripe回调方法！！");
        log.error("map具体内容：" + params.toString());
        // 提取参数
        String stripeId  = DataUtils.getStringVal(params, "stripeId");
        String sNo       = DataUtils.getStringVal(params, "sNo");
        String realSNo   = DataUtils.getStringVal(params, "realSNo");
        String eventType = DataUtils.getStringVal(params, "eventType");
        int    storeId   = Integer.parseInt(params.get("storeId").toString());

        String payNo = realSNo;
        //region 获取回调成功标识
        //        // 修改此处：从payload.data.object中获取payment_status
//        Map payload = (Map) params.get("payload");
//        Map data = (Map) payload.get("data");
//        Map object = (Map) data.get("object");
//        String paymentStatus = (String) object.get("payment_status");
        // 修改此处：使用FastJSON解析payload字符串
        String              payloadStr = (String) params.get("payload");
        Map<String, Object> payload    = null;
        try
        {
            // 使用FastJSON解析JSON字符串为Map
            payload = JSON.parseObject(payloadStr, new TypeReference<Map<String, Object>>()
            {
            });
        }
        catch (Exception e)
        {
            log.error("解析Stripe回调payload失败: {}", e.getMessage(), e);
            resultMap.put("code", ERROR_CODE_BBHDSB);
            resultMap.put("message", "解析回调数据失败");
            return resultMap;
        }

        // 安全获取嵌套Map
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null)
        {
            log.error("Stripe回调数据中缺少data字段");
            resultMap.put("code", ERROR_CODE_BBHDSB);
            resultMap.put("message", "回调数据格式错误");
            return resultMap;
        }

        Map<String, Object> object = (Map<String, Object>) data.get("object");
        if (object == null)
        {
            log.error("Stripe回调数据中缺少object字段");
            resultMap.put("code", ERROR_CODE_BBHDSB);
            resultMap.put("message", "回调数据格式错误");
            return resultMap;
        }

        // 从object中获取payment_status
        String paymentStatus = (String) object.get("payment_status");
        //endregion

        // 修正：从data.object.payment_intent中获取stripe_payment_intent
        String stripePaymentIntent = (String) object.get("payment_intent");
        log.info("从payment_intent字段获取到的stripe_payment_intent: " + stripePaymentIntent);

        String amount          = (String) params.get("mc_gross");
        String currency        = (String) params.get("mc_currency");
//        String paymentId       = (String) params.get("txn_id");
        String parentPaymentId = (String) params.get("parent_txn_id");
        log.info("商家订单号（payNo） = {}", payNo);
        log.info("订单状态 = {}", paymentStatus);
        log.info("金额 = {}", amount);
        log.info("币种 = {}", currency);
//        log.info("流水号 = {}", paymentId);
        if (parentPaymentId != null && !parentPaymentId.isEmpty())
        {
            log.info("父流水号 = {}", parentPaymentId);
        }

        // 判断是否支付成功
        logger.error("支付状态: {}", paymentStatus);
        if (!"paid".equals(paymentStatus))
        {
            log.error("支付失败,状态不为=paid");
            resultMap.put("code", ERROR_CODE_BBHDSB);
            resultMap.put("message", "error");
            return resultMap;
        }

        try
        {
            //region 根据不同的订单类型，存入stripe_payment_intent到不同的表中
            // 存入订单的stripe_payment_intent到lkt_order表
            int updateOrder = orderModelMapper.updateStripePaymentIntentBySno(sNo, stripePaymentIntent);
            log.info("在lkt_order修改了stripe_payment_intent " + updateOrder + " 条");

            // 店铺保证金操作（严格参照支付时的逻辑）
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                log.info("进入保证金订单，存入stripe_payment_intent（参照支付时逻辑）");

                // 1. 处理lkt_order_data表（与支付时新增/更新dataMap的逻辑对应）
                OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
                if (orderDataByTradeNo != null)
                {
                    // 解析原有data字段，补充stripe_payment_intent（对应支付时的dataMap）
                    String     dataJsonStr = orderDataByTradeNo.getData();
                    JSONObject dataJson    = JSONObject.parseObject(dataJsonStr);
                    dataJson.put("stripe_payment_intent", stripePaymentIntent); // 新增到data中，与stripe_id同级

                    OrderDataModel orderDataSave = new OrderDataModel();
                    orderDataSave.setData(dataJson.toJSONString());
                    orderDataSave.setTrade_no(sNo);
                    orderDataSave.setOrder_type("PR"); // 与支付时一致的订单类型

                    int row = orderDataModelMapper.updataByTradeNo(orderDataSave);
                    log.info("店铺保证金订单在lkt_order_data修改了stripe_payment_intent " + row + " 条");
                }

                // 2. 处理mch_promise表（与支付时新增/更新的逻辑对应）
                if (com.laiketui.core.utils.tool.StringUtils.isNotEmpty(sNo))
                {
                    // 先查询是否已有记录（对应支付时的mchPromiseOld判断）
                    MchPromiseModel mchPromiseOld = new MchPromiseModel();
                    mchPromiseOld.setOrderNo(sNo); // 用订单号查询，与支付时一致
                    mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);

                    MchPromiseModel mchPromiseSave = new MchPromiseModel();
                    mchPromiseSave.setStripe_payment_intent(stripePaymentIntent); // 新增字段

                    int row = 0;
                    if (mchPromiseOld == null)
                    {
                        // 若不存在，则不新增（因为支付时已插入，这里只更新）
                        log.info("店铺保证金订单的mch_promise记录不存在，无需新增");
                    }
                    else
                    {
                        // 若存在，则更新（对应支付时的update逻辑）
                        mchPromiseSave.setId(mchPromiseOld.getId());
                        row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseSave);
                    }
                    log.info("店铺保证金订单在mch_promise修改了stripe_payment_intent " + row + " 条");
                }
            }
            // 预售订单操作
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                log.info("进入预售订单，存入stripe_payment_intent");
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setsNo(sNo);
                preSellRecordModel.setPay_type(0);
                PreSellRecordModel preSellRecordModel1 = preSellRecordModelMapper.selectOne(preSellRecordModel);

                // 定金订单
                if (preSellRecordModel1 != null && StringUtils.isEmpty(preSellRecordModel1.getStripe_payment_intent()))
                {
                    preSellRecordModel1.setStripe_payment_intent(stripePaymentIntent);
                    int updatePs = preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel1);
                    log.info("预售定金订单存入了 " + updatePs + " 个stripe_payment_intent");
                }
                // 尾款订单
                else
                {
                    int updatePs = preSellRecordModelMapper.updateStripePaymentIntentBySNoAndPayType(sNo, stripePaymentIntent, 1);
                    log.info("预售尾款订单存入了 " + updatePs + " 个stripe_payment_intent");
                }
            }
            // 竞拍保证金操作
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                log.error("进入竞拍保证金订单，存入stripe_payment_intent");
                OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
                if (orderDataByTradeNo != null)
                {
                    String     dataJsonStr = orderDataByTradeNo.getData();
                    JSONObject dataJson    = JSONObject.parseObject(dataJsonStr);
                    dataJson.put("stripe_payment_intent", stripePaymentIntent);
                    orderDataByTradeNo.setData(dataJson.toJSONString());
                    int updateOrderData = orderDataModelMapper.updataByTradeNo(orderDataByTradeNo);
                    log.info("竞拍保证金订单在lkt_order_data修改了stripe_payment_intent " + updateOrderData + " 条");
                }
            }
            // 拼团订单操作
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
            {
                log.error("进入拼团订单，存入stripe_payment_intent");
                OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sNo);
                if (orderDataByTradeNo != null)
                {
                    String     dataJsonStr = orderDataByTradeNo.getData();
                    JSONObject dataJson    = JSONObject.parseObject(dataJsonStr);
                    dataJson.put("stripe_payment_intent", stripePaymentIntent);
                    orderDataByTradeNo.setData(dataJson.toJSONString());
                    int updateOrderData = orderDataModelMapper.updataByTradeNo(orderDataByTradeNo);
                    log.info("拼团订单在lkt_order_data修改了stripe_payment_intent " + updateOrderData + " 条");
                }
            }
            //endregion

//            int storeId = 0;
            //贝宝推送标题
            String title = "";
            logger.info("payBack-start-params-{}", JSON.toJSONString(params));

//            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
//            if (params.containsKey("paymentVo")) {
//                String paymentVoJson = (String) params.get("paymentVo");
//                PaymentVo paymentVo = JSON.parseObject(paymentVoJson, PaymentVo.class);
//                // 处理paymentVo对象
//            } else {
//                logger.error("支付回调参数中缺少paymentVo");
//                // 处理参数缺失的情况
//            }
//            String    paymentVoJson = (String) params.get("paymentVo");
//            PaymentVo paymentVo     = JSON.parseObject(paymentVoJson, PaymentVo.class);
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");


            logger.info("paymentVo-{}", paymentVo.toString());
            // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
//            String payNo = DataUtils.getStringVal(params, "custom");
            logger.error("payNo:" + payNo);

            //region 获取支付金额
            // Stripe 的 amount_total 是 Integer 类型（最小货币单位，如分），需转换为元
            Integer amountTotalInt = (Integer) object.get("amount_total");
            if (amountTotalInt == null)
            {
                log.error("Stripe回调数据中缺少amount_total字段");
                resultMap.put("code", ERROR_CODE_BBHDSB);
                resultMap.put("message", "回调数据缺少金额信息");
                return resultMap;
            }
            // 转换为 BigDecimal 并处理单位（分 -> 元，保留2位小数）
            BigDecimal total = new BigDecimal(amountTotalInt)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            //endregion

            if (com.laiketui.core.utils.tool.StringUtils.isEmpty(payNo) || total.compareTo(BigDecimal.ZERO) <= 0)
            {
                logger.error("普通订单回调失败信息 订单：{},支付金额：{}", payNo, total);
                resultMap.put("code", API_OPERATION_FAILED);
                resultMap.put("message", "error");
                return resultMap;
            }
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
//            String sNo = orderModelMapper.getOrderByRealSno(payNo);
//            logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
//            if (com.laiketui.core.utils.tool.StringUtils.isEmpty(sNo))
//            {
//                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
//                logger.info("先拆单后支付:{}", sNo);
//            }
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
            logger.info("stripe回调订单处理");
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
                    logger.error("stripe回调parameter参数错误:{}", paymentVo.getParameter());
                }
                String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                if (com.laiketui.core.utils.tool.StringUtils.isNotEmpty(laikeApiUrl))
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
                            throw new LaiKeAPIException(API_OPERATION_FAILED, "stripe支付失败", "wechatJsapiPay");
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
                        if (fzFlag != null && com.laiketui.core.utils.tool.StringUtils.isNotEmpty(fzFlag.toString()))
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
            logger.error("stripe回调失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "stripe回调失败", "payBack");
        }
        return resultMap;
    }

    //endregion

    //region 提现请求

    /**
     * Stripe 提现接口 - Java 8 完全兼容版
     */
    public Map<String, Object> payOut(StripeWithdrawVo vo) throws Exception
    {
        Map<String, Object> resultMap = new HashMap<>();
        User                user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
        logger.error("进入Stripe提现接口，参数: {}", vo.toString());

        try
        {
            Integer storeId = vo.getStoreId();
            //获取支付配置信息
            PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
            paymentConfigModel.setPid(16);
            paymentConfigModel.setStore_id(storeId);
            paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

            Map<String, Object> param = new HashMap<>(16);
            param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
            {
            });
            //获取Stripe API密钥
            String secretKey = MapUtils.getString(param, "secret_key");
            //设置Stripe API密钥
            Stripe.apiKey = secretKey;

            // 解析参数
            BigDecimal amount   = vo.getAmount();
            String     currency = vo.getCurrency() != null ? vo.getCurrency() : "usd";


//            user = userMapper.selectOne(user);
//            String stripeAccountId = user.getStripe_account_id();
            String stripeAccountId = vo.getStripeAccountId();

            // 刷新账户状态
            Account refreshedAccount = Account.retrieve(stripeAccountId);
            // ⚠️ 检查是否允许提现
            if (!refreshedAccount.getPayoutsEnabled())
            {
                logger.error("Stripe账户 {} 不允许提现，请检查账户设置或进行必要的身份验证", stripeAccountId);
                resultMap.put("code", ERROR_CODE_TXSBQSHZS);
                resultMap.put("message", "Stripe账户不允许提现，请检查账户设置或进行必要的身份验证");
                return resultMap;
            }


            // 金额转换
            long amountCents = amount.multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP).longValue();

            //  平台向connect子账户转账
            TransferCreateParams transferParams = TransferCreateParams.builder()
                    .setAmount(amountCents) // 单位: 分
                    .setCurrency(currency)
                    .setDestination(stripeAccountId)
                    .build();

            Transfer transfer = Transfer.create(transferParams);

            // 返回结果
            resultMap.put("code", SUCCESS);
            resultMap.put("message", "提现申请已提交");
            resultMap.put("created", transfer.getCreated());

        }
        catch (Exception e)
        {
            logger.error("Stripe提现失败: {}", e.getMessage(), e);
            throw new Exception("提现失败: " + e.getMessage());
        }

        return resultMap;
    }

    /**
     * Stripe 提现接口 - Java 8 完全兼容版
     */
    public Map<String, Object> payOut(String params) throws Exception
    {
        Map<String, Object> resultMap = new HashMap<>();
        logger.error("进入Stripe提现接口，参数: {}", params.toString());
        JSONObject jsonObject = JSON.parseObject(params);

        try
        {
            Integer storeId = Integer.valueOf(MapUtils.getString(jsonObject, "storeId"));
            //获取支付配置信息
            PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
            paymentConfigModel.setPid(16);
            paymentConfigModel.setStore_id(storeId);
            paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

            Map<String, Object> param = new HashMap<>(16);
            param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
            {
            });
            //获取Stripe API密钥
            String secretKey = MapUtils.getString(param, "secret_key");
            //设置Stripe API密钥
            Stripe.apiKey = secretKey;

            // 解析参数
            BigDecimal money    = (BigDecimal) jsonObject.get("money");//提现金额
            String     currency = "usd";


//            user = userMapper.selectOne(user);
//            String stripeAccountId = user.getStripe_account_id();
            String stripeAccountId = (String) jsonObject.get("stripe_account_id");

            // 刷新账户状态
            Account refreshedAccount = Account.retrieve(stripeAccountId);
            // ⚠️ 检查是否允许提现
            if (!refreshedAccount.getPayoutsEnabled())
            {
                logger.error("Stripe账户 {} 不允许提现，请检查账户设置或进行必要的身份验证", stripeAccountId);
                resultMap.put("code", ERROR_CODE_TXSBQSHZS);
                resultMap.put("message", "Stripe账户不允许提现，请检查账户设置或进行必要的身份验证");
                return resultMap;
            }


            // 金额转换
            long amountCents = money.multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP).longValue();

            //  平台向connect子账户转账
            TransferCreateParams transferParams = TransferCreateParams.builder()
                    .setAmount(amountCents) // 单位: 分
                    .setCurrency(currency)
                    .setDestination(stripeAccountId)
                    .build();

            Transfer transfer = Transfer.create(transferParams);

            // 返回结果
            resultMap.put("code", SUCCESS);
            resultMap.put("message", "提现申请已提交");
            resultMap.put("created", transfer.getCreated());

        }
        catch (Exception e)
        {
            logger.error("Stripe提现失败: {}", e.getMessage(), e);
            throw new Exception("提现失败: " + e.getMessage());
        }

        return resultMap;
    }


    /**
     * 用户绑定stripe连接子账号 以进行后续提现
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @throws StripeException
     */
    public Map<String, Object> bindStripeEmail(BindStripeEmailVo vo) throws LaiKeAPIException, StripeException
    {
        Map<String, Object> resultMap = new HashMap<>();
        Integer             storeId   = vo.getStoreId();
        //获取支付配置信息
        PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
        paymentConfigModel.setPid(16);
        paymentConfigModel.setStore_id(storeId);
        paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

        Map<String, Object> param = new HashMap<>(16);
        param = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
        {
        });
        //获取Stripe API密钥
        String secretKey = MapUtils.getString(param, "secret_key");

        //设置Stripe API密钥
        Stripe.apiKey = secretKey;

        //创建连接子账户的方法 stripe只能够向 连接子账户 进行转账
        AccountCreateParams params1 = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry("HK") // 设置国家为香港
                .setEmail(vo.getStripeEmail()) // 设置连接子账户的邮箱
                .build();

        Account account         = Account.create(params1);
        String  stripeAccountId = account.getId();
        System.out.println("Connected Account ID: " + account.getId());

        //向数据库存入用户的stripe连接子账号邮箱和stripe连接子账号ID
        User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
        user.setStripe_email(vo.getStripeEmail());
        user.setStripe_account_id(stripeAccountId);
        int i = userMapper.updateByPrimaryKeySelective(user);
        if (i < 1)
        {
            logger.error("更新用户Stripe信息失败，用户ID: {}, Stripe邮箱: {}, Stripe账户ID: {}", user.getUser_id(), user.getStripe_email(), user.getStripe_account_id());
            resultMap.put("code", ERROR_CODE_TXSBQSHZS);
            resultMap.put("message", "更新用户Stripe信息失败");
            return resultMap;
        }

        // 刷新账户状态
        Account refreshedAccount = Account.retrieve(stripeAccountId);

        // ⚠️ 检查是否允许提现
        if (!refreshedAccount.getPayoutsEnabled())
        {
            logger.error("Stripe账户 {} 不允许提现，请检查账户设置或进行必要的身份验证", stripeAccountId);
            resultMap.put("code", ERROR_CODE_TXSBQSHZS);
            resultMap.put("message", "Stripe账户不允许提现，请检查账户设置或进行必要的身份验证");
            return resultMap;
        }

        resultMap.put("code", SUCCESS);
        resultMap.put("stripe_account_id", stripeAccountId);

        return resultMap;
    }


    // 测试方法 提现
    public static void main(String[] args)
    {
        // 设置你的Stripe测试密钥
        Stripe.apiKey = "sk_test_51RgKgaRoDsISw3pRFUL4bMy8p2QLe0pEZIDRhOeOAaPjodHucynCPDoeV3g35cJKF8FLMLwIMa3APuxvLCjZu9Je00b2Zf739s";

        try
        {
            // 测试参数（请替换为有效数据）
            long   amountCents          = 1000; // 10美元（单位：美分）
            String currency             = "usd";
            String destinationAccountId = "acct_1Ri8MC2LNOaRCJMU"; // 替换为有效连接账户ID

            // 创建转账参数
            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency)
                    .setDestination(destinationAccountId)
                    .build();

            // 执行转账
            Transfer transfer = Transfer.create(params);

            // 构建OAuth URL
//            // 准备 OAuth 参数
//            Map<String, Object> params2 = new HashMap<>();
//            params2.put("redirect_uri", "https://your-app.com/oauth/callback");
//            params2.put("scope", "read_write");
//            params2.put("state", "random_state_value"); // 用于CSRF保护
//
//            try {
//                // 构建授权 URL
//                String oauthUrl = OAuth.authorizeUrl(params2, null);
//                System.out.println("Authorization URL: " + oauthUrl);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            // 打印结果
            System.out.println("转账创建成功！");
            System.out.println("Transfer ID: " + transfer.getId());
            System.out.println("金额: " + transfer.getAmount() + " " + transfer.getCurrency());
            System.out.println("目标账户: " + transfer.getDestination());

        }
        catch (StripeException e)
        {
            // 处理Stripe API错误
            System.err.println("Stripe API错误: " + e.getCode());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // 处理其他异常
            System.err.println("发生未知错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //endregion


}
