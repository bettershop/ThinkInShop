package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.payment.CompsAlipayService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.config.alipay.AlipayConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 支付宝支付
 *
 * @author wangxian
 */
@Service
public class CompsAlipayServiceDubboImpl extends CompsPayServiceAdapter implements CompsAlipayService
{

    private final Logger logger = LoggerFactory.getLogger(CompsAlipayServiceDubboImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    /**
     * app支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> aliAppPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newConcurrentMap();
        try
        {
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            User      user      = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            // 参数
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付失败", "pay");
            }

            //获取订单信息
            OrderModel orderModel  = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());
            String     subject     = paymentVo.getTitle();
            String     outTradeNo  = orderModel.getReal_sno();
            BigDecimal totalAmount = orderModel.getZ_price();
//            AlipayTradeQueryResponse tradeQueryResponse = Factory.Payment.Common().query(outTradeNo);
//            logger.info("aliAppPay 支付结果：{}", JSON.toJSONString(tradeQueryResponse));
            // 1. 发起API调用
            AlipayTradeAppPayResponse response = Factory.Payment.App().pay(subject, outTradeNo, String.valueOf(totalAmount));
            logger.info("支付宝app支付信息:{}", response.toMap());
            // 2. 处理响应或异常
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                logger.info("支付宝手机app调用成功");
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliAppPay");
            }
        }
        catch (Exception e)
        {
            logger.error("app支付错误：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliAppPay");
        }
        return resultMap;
    }

    /**
     * app支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> aliMiNiPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newConcurrentMap();
        try
        {
            PaymentVo paymentVo         = (PaymentVo) params.get("paymentVo");
            User      user              = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            String    paymentConfigInfo = paymentConfigModelMapper.getPaymentConfigInfo(paymentVo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY);
            if (StringUtils.isEmpty(paymentConfigInfo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝小程序配置信息不存在", "aliMiNiPay");
            }
            Map<String, Object> map = JSON.parseObject(paymentConfigInfo, new TypeReference<Map<String, Object>>()
            {
            });
            logger.info("支付宝小程序支付配置信息:::::paymentConfigInfo{}", map);
            //参数准备
            BigDecimal total              = paymentVo.getTotal();
            String     title              = paymentVo.getTitle();
            String     sno                = paymentVo.getsNo();
            OrderModel orderInfo          = publicOrderService.getOrderInfo(sno, paymentVo, user.getUser_id());
            String     authCode           = paymentVo.getAlimp_authcode();
            String     appId              = MapUtils.getString(map, "appid");//appId
            String     notifyUrl          = MapUtils.getString(map, "notify_url");//签名方式
            String     signType           = MapUtils.getString(map, "signType");//签名方式
            String     rsaPrivateKey      = MapUtils.getString(map, "rsaPrivateKey");//支付宝私钥
            String     alipayrsaPublicKey = MapUtils.getString(map, "alipayrsaPublicKey");//支付宝公钥
            //获取用户支付宝id
//            String aLiPayUserInfo = AliPayUtil.getALiPayUserInfo(authCode, appId, signType, rsaPrivateKey, alipayrsaPublicKey);
//            JSONObject jsonObject = JSONObject.parseObject(aLiPayUserInfo);
//            String userId = jsonObject.getString("user_id");
            String userId = user.getZfb_id();
            logger.info("支付宝小程序支付信息开始-----------userId{}", userId);
            logger.info("用户绑定的支付宝id-----------zfbId{}", user.getZfb_id());
            rsaPrivateKey = rsaPrivateKey.replaceAll("%2B", "\\+");
            rsaPrivateKey = rsaPrivateKey.replaceAll("%2F", "\\/");
            rsaPrivateKey = rsaPrivateKey.replaceAll("%3D", "\\=");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%2B", "\\+");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%2F", "\\/");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%3D", "\\=");
            AlipayClient             alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, rsaPrivateKey, "json", "GBK", alipayrsaPublicKey, signType);
            AlipayTradeCreateRequest request      = new AlipayTradeCreateRequest();
            request.setNotifyUrl(notifyUrl);
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderInfo.getReal_sno());
            bizContent.put("total_amount", total);
            bizContent.put("subject", title);
            bizContent.put("buyer_id", userId);
            bizContent.put("timeout_express", "10m");
            request.setBizContent(bizContent.toJSONString());
            AlipayTradeQueryResponse tradeQueryResponse = Factory.Payment.Common().query(orderInfo.getReal_sno());
            logger.info("aliMiNiPay 支付结果：{}", JSON.toJSONString(tradeQueryResponse));
            AlipayTradeCreateResponse response = alipayClient.execute(request);
            logger.info("支付宝app支付信息:{}", response.toString());
            // 2. 处理响应或异常
            if (response.isSuccess())
            {
                resultMap.put("outTradeNo", response.getOutTradeNo());
                resultMap.put("tradeNo", response.getTradeNo());
                logger.info("支付宝小程序支付调用成功");
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝小程序支付出错", "aliMiNiPay");
            }
        }
        catch (Exception e)
        {
            logger.error("app支付错误：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝小程序支付出错", "aliMiNiPay");
        }
        return resultMap;
    }

    /**
     * 手机浏览器支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> aliWapPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newConcurrentMap();
        try
        {
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            User      user      = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            logger.info("传入信息：{}", params);
            // 参数
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付失败", "pay");
            }

            //获取订单信息
            OrderModel orderModel = new OrderModel();
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(sNo);
                OrderDataModel orderDataModel1 = orderDataModelMapper.selectOne(orderDataModel);
                MemberOrderVo  memberOrderVo   = JSONObject.parseObject(orderDataModel1.getData(), MemberOrderVo.class);
                orderModel.setReal_sno(orderDataModel1.getTrade_no());
                orderModel.setZ_price(memberOrderVo.getAmount());
                orderModel.setStore_id(memberOrderVo.getStoreId());
            }
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                logger.info("拼团订单走这：：：：：：：：");
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(sNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                Map<String, Object> map = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                {
                });
                orderModel.setReal_sno(orderDataModel.getTrade_no());
                orderModel.setZ_price(new BigDecimal(map.get("z_price").toString()));
                orderModel.setStore_id(Integer.valueOf(map.get("store_id").toString()));
            }
            else
            {
                orderModel = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());
            }
            logger.info("订单信息：：：：：：{}",JSON.toJSONString(orderModel));
            // 1. 发起API调用
            String title = "";
            if (StringUtils.isEmpty(paymentVo.getTitle()))
            {
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setR_sNo(paymentVo.getsNo());
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                logger.info("订单数据：：：：：{}",JSON.toJSONString(orderDetailsModelList));
                title = orderDetailsModelList.get(0).getP_name();
            }
            else
            {
                title = paymentVo.getTitle();
            }
            String     subject     = title;
            logger.info("subject:::::::{}",subject);
            String     outTradeNo  = orderModel.getReal_sno();
            logger.info("支付订单号：：：：：：：{}",outTradeNo);
            BigDecimal totalAmount = orderModel.getZ_price();
            logger.info("支付金额：：：：：：：{}",totalAmount);
            AlipayTradeQueryResponse tradeQueryResponse = Factory.Payment.Common().query(outTradeNo);
            logger.info("aliWapPay 支付结果：{}", JSON.toJSONString(tradeQueryResponse));
            String                    h5Url     = getH5Url(orderModel.getStore_id()) + "pages/pay/payResult?sNo=" + orderModel.getReal_sno() + "&payment_money=" + orderModel.getZ_price() + "&isH5=true";
            logger.info("h5Url::::::::{}",h5Url);
            String                    qiutUrl   = DataUtils.getStringVal(params, "qiutUrl", getH5Url(orderModel.getStore_id()));
            logger.info("qiutUrl::::::::{}",qiutUrl);
            String                    returnUrl = DataUtils.getStringVal(params, "returnUrl", h5Url);
            logger.info("returnUrl::::::::{}",returnUrl);
            AlipayTradeWapPayResponse response  = Factory.Payment.Wap().pay(subject, outTradeNo, totalAmount.toString(), qiutUrl, returnUrl);
            logger.info("支付宝支付响应数据：：：：：：：{}",JSON.toJSONString(response));
            // 2. 处理响应或异常
            if (ResponseChecker.success(response))
            {
                resultMap.put("data", response.getBody());
                logger.info("返回记录{}", JSON.toJSONString(resultMap));
                logger.info("支付宝手机网站调用成功");
            }
            else
            {
                logger.info("支付宝支付错误信息：：：：：：：{}",response.getBody());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliWapPay",response.getBody());
            }
        }
        catch (Exception e)
        {
            logger.error("app支付错误：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliWapPay");
        }
        return resultMap;
    }

    @Autowired
    private ConfigModelMapper configModelMapper;

    /**
     * 获取H5地址
     *
     * @param storeId
     * @return
     */
    public String getH5Url(int storeId)
    {
        String h5Url = "";
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            h5Url = configModel.getH5_domain();
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        return h5Url;
    }


    /**
     * pc扫码支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> aliPcQrPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newConcurrentMap();
        try
        {
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            User      user      = getUser(paymentVo);
            // 参数
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
            logger.info("aliPcQrPay 获取sno：{}", sNo);
            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付失败", "pay");
            }

            //获取订单信息
            OrderModel orderModel = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());
            String     subject    = paymentVo.getTitle();
            String     outTradeNo = orderModel.getReal_sno();
            logger.info("aliPcQrPay 获取outTradeNo：{}", outTradeNo);
            //AlipayTradeQueryResponse tradeQueryResponse = Factory.Payment.Common().query(outTradeNo);
            //logger.info("aliPcQrPay 支付结果：{}", JSON.toJSONString(tradeQueryResponse));

            BigDecimal                   totalAmount = orderModel.getZ_price();
            AlipayTradePrecreateResponse response    = Factory.Payment.FaceToFace().preCreate(subject, outTradeNo, totalAmount.toString());
            logger.info("支付宝app支付信息:{}", response.toMap());
            // 2. 处理响应或异常
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                logger.info("支付宝pc扫码支付调用成功");
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliPcQrPay");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("app支付错误：{}", e.getMessage());
            logger.error(JSON.toJSONString(e));
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "aliPcQrPay");
        }
        return resultMap;
    }

    private User getUser(PaymentVo paymentVo)
    {
        User user;
        if (GloabConst.StoreType.STORE_TYPE_PC_MALL == paymentVo.getStoreType())
        {
            logger.debug("pc商城支付");
            user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
        }
        else
        {
            user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
        }
        return user;
    }

    /**
     * 支付入口
     *
     * @param params
     * @return alipay_mobile
     * @throws LaiKeAPIException
     */
    @Transactional
    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        logger.info("支付宝参数：{}", JSONObject.toJSONString(params));
        Map<String, Object> resultMap = Maps.newConcurrentMap();
        logger.info("传入信息：{}", JSONObject.toJSONString(params));
        try
        {
            String payType = init(params);
            switch (payType)
            {
                case "alipay":
                    resultMap = this.aliAppPay(params);
                    break;
                case "alipay_mobile":
                    resultMap = this.aliWapPay(params);
                    break;
                case "pc_alipay":
                    resultMap = this.aliPcQrPay(params);
                    break;
                case "alipay_minipay":
                    resultMap = this.aliMiNiPay(params);
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("app支付错误：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "pay");
        }
        return resultMap;
    }

    /**
     * 初始化方法
     *
     * @param params
     * @return
     */
    private String init(Map<String, Object> params)
    {
        try
        {
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            int       storeId   = paymentVo.getStoreId();
            String    payType   = paymentVo.getPayType();

            // 支付配置信息 全部存为json 格式 配置信息
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, payType);
            Config config    = AlipayConfigInfo.getOptions(configStr);
            if (config == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "pay");
            }
            Factory.setOptions(config);
            return payType;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("初始化失败,{}", e.getMessage());
            throw new LaiKeAPIException("", "初始化失败", "init");
        }

    }

    /**
     * 支付宝退款
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Transactional
    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            String                    payType     = init(params);
            String                    outTradeNo  = DataUtils.getStringVal(params, "outTradeNo", "来客电商商品");
            BigDecimal                totalAmount = DataUtils.getBigDecimalVal(params, "totalAmount", new BigDecimal("0.01"));
            AlipayTradeRefundResponse response    = Factory.Payment.Common().refund(outTradeNo, totalAmount.toString());
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                logger.info("支付宝退款调用成功");
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝退款出错", "refund");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("app支付错误：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝支付出错", "pay");
        }
        return resultMap;
    }


    private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 30, 2000,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    /**
     * 支付宝回调认证
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = null;
        try
        {
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            int       storeId   = paymentVo.getStoreId();
            logger.info("paymentVo", JSONObject.toJSONString(paymentVo));
            logger.info("回调参数{}", params);
            logger.info("回调初始化成功。{}", JSONObject.toJSONString(params));
            String payNo       = DataUtils.getStringVal(params, "out_trade_no");
            String totalAmount = DataUtils.getStringVal(params, "total_amount");
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用调起支付所用订单号和总订单相同)
            String sNo = orderModelMapper.getOrderByRealSno(payNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
            if (StringUtils.isEmpty(sNo))
            {
                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
            }
            //钱包充值sNo = null
            if (sNo == null)
            {
                sNo = payNo;
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            logger.info("支付宝回调订单处理");
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackUpOrderMember(orderDataModel);
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                /**更新订单*/
                resultMap = publicOrderService.payBackForMember(orderDataModel);
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                logger.info("开始支付预售订单流程");
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setReal_sno(payNo);
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                //只有支付定金时需要用到
                paymentVo.setPayment_money(totalAmount);
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
                    paymentVo.setPayTarget(1);
                }
                if (paymentVo.getPayTarget().equals(1))
                {
                    orderModel = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                }
                else
                {
                    orderModel = new OrderModel();
                    orderModel.setsNo(preSellRecordModel.getsNo());
                    orderModel = orderModelMapper.selectOne(orderModel);
                }
                resultMap = publicOrderService.payBackForPreSell(paymentVo, orderModel);
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                logger.info("开始支付保证金流程");
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderMchPromise(orderDataModel);
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(payNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                resultMap = publicOrderService.payBackUpOrderAuctionPromise(orderDataModel);
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
                    logger.debug("支付宝回调parameter参数错误:{}", paymentVo.getParameter());
                }
                //请参照 plugin.auction.payCallBack
                String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                if (StringUtils.isNotEmpty(laikeApiUrl))
                {
                    if (paymentVo.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                    {
                        logger.info("拼团订单支付宝回调开始：：：：：：：：：：：：：：：：：：：：：：：：：：");

                        logger.info("拼团支付信息：：：：：：：：：：：：：：{}",JSON.toJSONString(paymentVo));
                        //获取临时表订单数据
                        OrderDataModel orderDataModel = new OrderDataModel();
                        orderDataModel.setPay_type(paymentVo.getPayType());
                        orderDataModel.setTrade_no(paymentVo.getsNo());
                        orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                        if (orderDataModel == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
                        }
                        //下单
                        OrderVo orderVo = new OrderVo();
                        orderVo.setStoreId(storeId);
                        orderVo.setUserId(paymentVo.getUserId());
                        orderVo.setStoreType(paymentVo.getStoreType());
                        orderVo.setProductsInfo(orderDataModel.getData());
                        logger.info("下单信息：：：：：：：：：{}",JSON.toJSONString(orderVo));
                        Map<String, Object> paramApiMap    = JSON.parseObject(JSON.toJSONString(orderVo));
                        Map<String, Object> resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.app.payment", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                            logger.info("plugin.group.app.payment远程调用返回值: " + JSON.toJSONString(resultMapOrder));
                        paymentVo.setsNo(MapUtils.getString(resultMapOrder, "sNo"));


                        //标记临时订单已支付
                        OrderDataModel orderDataUpdate = new OrderDataModel();
                        orderDataUpdate.setId(orderDataModel.getId());
                        orderDataUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                        orderDataModelMapper.updateByPrimaryKeySelective(orderDataUpdate);
                    }
                    logger.debug("正在支付订单{} 执行接口:{}", payNo, laikeApiUrl);
                    Map<String, Object> paramMap1 = new HashMap<>(1);
                    //paramMap1.put("paramJson", JSON.toJSONString(paymentVo));
                    paramMap1.put("vo", JSON.toJSONString(paymentVo));
                    resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramMap1,MediaType.MULTIPART_FORM_DATA_VALUE);
                    logger.debug("支付回调成功 返回数据{}", JSON.toJSONString(resultMap));
                }
                else if (orderModel != null)
                {
                    //更新订单
                    resultMap = publicOrderService.payBackUpOrder(orderModel);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("支付宝回调异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付宝回调出错", "pay");
        }
        return resultMap;
    }
}
