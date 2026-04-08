package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.weixin.AppletUtil;
import com.laiketui.common.utils.weixin.DeviceUtils;
import com.laiketui.common.utils.weixin.WXPaySignatureCertificateUtil;
import com.laiketui.common.utils.weixin.WXPayV3Constants;
import com.laiketui.comps.api.payment.CompsWechatPayService;
import com.laiketui.comps.payment.common.v3Dto.CompsMerchantConfig;
import com.laiketui.comps.payment.util.CompsWechatPayUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.IpUtil;
import com.laiketui.core.utils.tool.WxOpenIdUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.distribution.DistributionWithdrawModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.message.TemplateData;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.root.common.BuilderIDTool;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;
import com.wechat.pay.java.service.transferbatch.model.*;
import github.wxpay.sdk.WXPayUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.laiketui.core.lktconst.DictionaryConst.OrderPayType.*;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.API_OPERATION_FAILED;


/**
 * 微信支付
 *
 * @author wangxian
 */
@Service("compsWechatPayServiceV3DubboImpl")
@RefreshScope
public class CompsWechatPayServiceV3DubboImpl extends CompsPayServiceAdapter implements CompsWechatPayService
{

    private final Logger logger = LoggerFactory.getLogger(CompsWechatPayServiceV3DubboImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private CompsMerchantConfig merchantConfig;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PubliceService publiceService;


    @Autowired
    private DistributionWithdrawModelMapper distributionWithdrawModelMapper;

    /**
     * 初始化
     *
     * @param params
     * @throws IOException
     */
    public CloseableHttpClient setup(Map params) throws IOException
    {
        /** 加载商户私钥（privateKey：私钥字符串）*/
        String     privateKey         = MapUtils.getString(params, "key_pem");
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes(GloabConst.Chartset.UTF_8)));
        /** 加载平台证书（mchId：商户号,mchSerialNo：商户证书序列号,apiV3Key：V3秘钥）*/
        String mchId       = MapUtils.getString(params, "mch_id");
        String certContext = MapUtils.getString(params, "cert_pem");
        String mchSerialNo = getSerialNo(certContext);
        // TODO json新增序列号apiV3Key
        String apiV3Key = MapUtils.getString(params, "apiV3Key");
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, merchantPrivateKey)), apiV3Key.getBytes(GloabConst.Chartset.UTF_8));
        return WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(verifier)).build();
    }

    /**
     * 创建订单 H5/APP/Native/JSAPI/
     *
     * @param params
     * @throws Exception
     */
    public String createOrder(Map params) throws Exception
    {
        logger.info("微信支付下单apiv3:{}", params);
        CloseableHttpClient   httpClient = setup(params);
        CloseableHttpResponse response   = null;
        try
        {
            //请求URL
            String apiUrl      = DataUtils.getStringVal(params, "apiUrl");
            String description = DataUtils.getStringVal(params, "description");
            String mchId       = DataUtils.getStringVal(params, "mchid");
            String appId       = DataUtils.getStringVal(params, "appid");
            String notifyUrl   = DataUtils.getStringVal(params, "notify_url");
            String payOrderNo  = DataUtils.getStringVal(params, "payOrderNo");
            int    total       = DataUtils.getIntegerVal(params, "total", 1) * 100;

            /** 支付类型 jsapi和小程序 才有 */
            String payClass = DataUtils.getStringVal(params, "payClass");
            String payer    = "";
            if (ORDERPAYTYPE_MINI_WECHAT.equals(payClass) ||
                    ORDERPAYTYPE_JSAPI_WECHAT.equals(payClass))
            {
                String openid = DataUtils.getStringVal(params, "openid");
                payer = "\"payer\": {"
                        + "\"openid\": \"" + openid + "\"" + "},";
            }

            HttpPost httpPost = new HttpPost(apiUrl);
            // 请求body参数
            String reqdata = "{"
                    + "\"amount\": {"
                    + "\"total\": " + total + ","
                    + "\"currency\": \"CNY\""
                    + "},"
                    + "\"mchid\": \"" + mchId + "\","
                    + "\"description\": \"" + description + "\","
                    + "\"notify_url\": \"" + notifyUrl + "\","
                    + payer
                    + "\"out_trade_no\": \"" + payOrderNo + "\","
                    + "\"appid\": \"" + appId + "\" }";

            StringEntity entity = new StringEntity(reqdata);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            //完成签名并执行请求
            response = httpClient.execute(httpPost);
            String resultStr  = EntityUtils.toString(response.getEntity());
            int    statusCode = response.getStatusLine().getStatusCode();
            logger.info("微信支付返回信息,响应code = {},响应body = ", statusCode, resultStr);
            if (statusCode == 200 || statusCode == 204)
            {
                return resultStr;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "createOrder");
            }
        }
        finally
        {
            response.close();
            httpClient.close();
            logger.info("微信支付下单apiv3结束！");
        }
    }

    /**
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> wechatAppPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            String mchId = DataUtils.getStringVal(params, "mchid");
            String appId = DataUtils.getStringVal(params, "appid");

            JSONObject resultJson = JSONObject.parseObject(createOrder(params));
            /** 获取预支付订单id */
            String prepayId = resultJson.getString("prepay_id");

            if (StringUtils.isNotEmpty(prepayId))
            {
                /**结果处理*/
                resultMap.put("appid", appId);
                resultMap.put("partnerid", mchId);
                resultMap.put("prepayid", prepayId);
                resultMap.put("package", "Sign=WXPay");
                String nonceStr = WXPayUtil.generateNonceStr();
                resultMap.put("noncestr", nonceStr);
                Long timestamp = System.currentTimeMillis() / 1000;
                resultMap.put("timestamp", timestamp);
                String signatureStr = Stream.of(appId, String.valueOf(timestamp), nonceStr, prepayId)
                        .collect(Collectors.joining("\n", "", "\n"));
                String paySign = CompsWechatPayUtils.sign(params, signatureStr);
                resultMap.put("paySign", paySign);
                return resultMap;
            }
            else
            {
                String message = resultJson.getString("message");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, message, "wechatAppPay");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatAppPay");
        }
    }


    /**
     * H5支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> weChatWapPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //异常情况下的结果处理
            resultMap.put("data", createOrder(params));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "weChatWapPay");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> wechatPcQrPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap.put("data", createOrder(params));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatPcQrPay");
        }
        return resultMap;
    }

    /**
     * 小程序支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> wechatMiniPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            resultMap = wechatAppPay(params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatMiniPay");
        }
        return resultMap;
    }

    /**
     * jsapi支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> wechatJsapiPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            resultMap = (params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    /**
     * 此方法在这里做分发
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Transactional
    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            logger.info("微信v3支付");
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            User user = null;
            if (GloabConst.StoreType.STORE_TYPE_PC_MALL == paymentVo.getStoreType())
            {
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            }
            logger.info("传入信息：{}", JSONObject.toJSONString(paymentVo));
            // 参数
            String orderList = paymentVo.getOrder_list();
            JSONObject jsonObject = JSONObject.parseObject(orderList);
            // 订单sNo
            String sNo;
            if (!StringUtils.isEmpty(paymentVo.getsNo()))
            {
                sNo = paymentVo.getsNo();
            } else
            {
                sNo = jsonObject.getString("sNo");
            }
            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "支付失败", "pay");
            }
            //获取微信支付配置
            String className = paymentVo.getPayType();
            int storeId = paymentVo.getStoreId();
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            logger.info(className + "支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String appId = payJson.getString("appid");
            logger.info("app_id:{}", appId);
            String mchId = payJson.getString("mch_id");
            logger.info("mch_id:{}", mchId);
            String notifyUrl = payJson.getString("notify_url");
            String keyPem = payJson.getString("key_pem");
            logger.info("key_pem:{}", keyPem);
            String serial_no =  payJson.getString("serial_no");
            logger.info("序列号证书：{}", serial_no);
            String appSecreKey = payJson.getString("appsecret");
            logger.info("appSecreKey:{}", appSecreKey);

            logger.info("code:{}",paymentVo.getCode());

            //获取订单信息
            OrderModel orderModel = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());
            //支付类型
            String payClass = paymentVo.getType();
            //下单调用路径
            String httpUrl = null;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = objectMapper.createObjectNode();
            //公共参数
                rootNode.put("appid", appId)//服务商APPID
                    .put("mchid", mchId)//服务商户号
                    .put("description", Objects.isNull(paymentVo.getTitle()) ? "微信支付" : paymentVo.getTitle())//商品描述
                    .put("out_trade_no", orderModel.getReal_sno())//支付订单号
                    .put("notify_url", notifyUrl);//回调地址
            rootNode.putObject("amount")//订单金额信息
                    .put("total", orderModel.getZ_price().multiply(new BigDecimal(100)).intValue())//订单总金额(单位为分)
                    .put("currency", "CNY");//人民币

            switch (payClass)
            {
                //微信小程序支付
                case ORDERPAYTYPE_MINI_WECHAT:
                case ORDERPAYTYPE_JSAPI_WECHAT:
                    String openid = null;
                    String code      = paymentVo.getCode();
                    logger.info("code:{}", code);
                    if (payClass.equals(ORDERPAYTYPE_MINI_WECHAT))
                    {
                        if (StringUtils.isNotEmpty(paymentVo.getOpenid()))
                        {
                            openid = paymentVo.getOpenid();
                        }
                        else
                        {
                            openid = WxOpenIdUtils.getMiniOpenid(appId, appSecreKey, code);
                        }
                    }
                    else
                    {
                       openid = WxOpenIdUtils.getGzhOpenid(appId, appSecreKey, code);
                    }
                    logger.info("openId:{}", openid);

                    httpUrl = WXPayV3Constants.DOMAIN_API + WXPayV3Constants.PAY_TRANSACTIONS_JSAPI;
                    rootNode.putObject("payer")//支付者信息
                            .put("openid", openid);//微信openid
                    break;
                //手机h5支付
                case ORDERPAYTYPE_H5_WECHAT:
                    httpUrl = WXPayV3Constants.DOMAIN_API + WXPayV3Constants.PAY_TRANSACTIONS_H5;
                    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                    String deviceType = DeviceUtils.getDeviceType(request);
                    logger.info("deviceType:{}", deviceType);

                    rootNode.putObject("scene_info")//场景信息
                            .put("payer_client_ip",IpUtil.getIpAddr(request)) //ip地址
                            .putObject("h5_info") //h5场景信息
                            .put("type", deviceType); //型号 Wap、iOS、Android
                    break;
                //pc商城支付（二维码支付）
                case ORDERPAYTYPE_PC_WECHAT:
                    httpUrl = WXPayV3Constants.DOMAIN_API + WXPayV3Constants.PAY_TRANSACTIONS_PC;
                    break;
                //app支付
                case ORDERPAYTYPE_APP_WECHAT:
                    httpUrl = WXPayV3Constants.DOMAIN_API + WXPayV3Constants.PAY_TRANSACTIONS_APP;
                    break;
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault())
            {
                HttpPost httpPost = new HttpPost(httpUrl);
                //时间戳
                String timestamp = String.valueOf((System.currentTimeMillis() / 1000));
                //随机字符串 32位
                String nonceStr = RandomStringUtils.randomAlphanumeric(32);
                logger.info("httpPost.getMethod:{}",httpPost.getMethod());
                logger.info("httpPost.getURI:{}",httpPost.getURI().getPath());
                String authorization = WXPaySignatureCertificateUtil.buildAuthorization(mchId, serial_no, httpPost.getMethod(), httpPost.getURI().getPath(), rootNode.toString(), timestamp, nonceStr, keyPem);
                WXPaySignatureCertificateUtil.wxV3Post(bos, objectMapper, rootNode, httpPost, authorization);
                try
                {
                    CloseableHttpResponse response = httpClient.execute(httpPost);
                    //获取返回状态
                    int statusCode = response.getStatusLine().getStatusCode();
                    logger.info("statusCode:{}", statusCode);
                    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                    logger.info("result:{}", result);
                    //请求成功
                    if (statusCode == 200)
                    {
                        JSONObject resultJson = JSONObject.parseObject(result);
                        logger.info("resultJson:{}", resultJson);
                        String prepayId = resultJson.getString("prepay_id");

                        String paySign;
                        Map<String, String> payData = new HashMap<>();
                        switch (payClass)
                        {
                            case ORDERPAYTYPE_APP_WECHAT:
                                //生成带签名支付信息
                                paySign = WXPaySignatureCertificateUtil.appPaySign(appId,timestamp, nonceStr, prepayId,keyPem);
                                payData.put("appid",appId);
                                payData.put("partnerid",mchId);
                                payData.put("prepayid",prepayId);
                                payData.put("package", "Sign=WXPay");
                                payData.put("noncestr",nonceStr);
                                payData.put("timestamp",timestamp);
                                payData.put("sign",paySign);
                                break;
                            case ORDERPAYTYPE_PC_WECHAT:
                                //二维码链接
                                String code_url = resultJson.getString("code_url");
                                payData.put("code_url",code_url);
                                logger.info("code_url:{}", code_url);
                                break;
                            case ORDERPAYTYPE_MINI_WECHAT:
                            case ORDERPAYTYPE_JSAPI_WECHAT:
                                payData.put("timeStamp", timestamp);
                                payData.put("state", String.valueOf(1));
                                payData.put("appid", appId);
                                payData.put("nonceStr",nonceStr);
                                payData.put("signType", "RSA");
                                payData.put("package", "prepay_id=" + prepayId);
                                paySign = WXPaySignatureCertificateUtil.appPaySign(appId,timestamp, nonceStr, prepayId,keyPem);
                                logger.info("paySign:{}", paySign);
                                payData.put("paySign", paySign);
                                payData.put("out_trade_no", orderModel.getReal_sno());
                                break;
                            case ORDERPAYTYPE_H5_WECHAT:
                                String url = resultJson.getString("h5_url");
                                String h5Url = "";
                                ConfigModel configModel = new ConfigModel();
                                configModel.setStore_id(storeId);
                                configModel = configModelMapper.selectOne(configModel);
                                h5Url = configModel.getH5_domain();
                                h5Url = h5Url + "pages/pay/payResult?sNo=" + orderModel.getReal_sno() + "&payment_money=" + orderModel.getZ_price() + "&isH5=true";
                                String gbk = URLEncoder.encode(h5Url, "GBK");
                                payData.put("url", url + "&redirect_url=" + gbk);
                                payData.put("pay_type", payClass);
                                payData.put("prepayid",prepayId);
                                break;
                        }
                        logger.info("返回参数：：{}",JSONObject.toJSONString(payData));
                        resultMap.put("data", payData);
                        resultMap.put("code", 200);
                        resultMap.put("message", "支付成功");
                    }
                    else
                    {
                        resultMap.put("code", 500);
                        resultMap.put("message", "支付失败");
                    }
                }
                catch (Exception e)
                {
                    logger.error("微信支付失败:{}",e.getMessage());
                }

            }catch (LaiKeAPIException e)
            {
                logger.error("支付失败",e);
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("微信支付失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException
    {
        Map resultMap = new HashMap<>(16);
        try
        {
            PaymentVo paymentVo   = (PaymentVo) params.get("paymentVo");
            String    className   = paymentVo.getType();
            int       storeId     = paymentVo.getStoreId();
            String    paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            paymentJson = paymentJson.replaceAll("%2B", "\\+");
            if (StringUtils.isEmpty(paymentJson))
            {
                logger.error("获取支付信息");
                throw new LaiKeAPIException(API_OPERATION_FAILED, "获取支付信息", "getPayConfig");
            }
            logger.info(className + "支付信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            resultMap.put("data", payJson);
        }
        catch (Exception e)
        {
            logger.error("获取支付信息", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "获取支付信息", "getPayConfig");
        }
        return resultMap;
    }

    @Transactional
    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            logger.info("v3支付回调");
            int storeId = 0;
            String sNo = null;
            String title = "";
            storeId = MapUtils.getInteger(params, "storeId");
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");

            String payNo = DataUtils.getStringVal(params, "payNo");
            BigDecimal total = DataUtils.getBigDecimalVal(params, "total");
            if (StringUtils.isEmpty(payNo) || total.compareTo(BigDecimal.ZERO) <= 0)
            {
                logger.error("普通订单回调失败信息 订单：{},支付金额：{}", payNo, total);
                resultMap.put("code", ErrorCode.BizErrorCode.API_OPERATION_FAILED);
                resultMap.put("message", "error");
                return resultMap;
            }
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
            sNo = orderModelMapper.getOrderByRealSno(payNo);
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
            logger.info("微信版本3回调订单处理");
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
                    logger.error("微信回调parameter参数错误:{}", paymentVo.getParameter());
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
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
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
                        if (!select.isEmpty())
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
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(paymentVo.getStoreId());
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", paymentVo.getStoreId());
            }
            //发起人openid
            String openId = MapUtils.getString(params, "openId");
            if (StringUtils.isEmpty(openId))
            {
                openId = MapUtils.getString(params, "openid");
            }
            logger.error("openId: {}", openId);
            //发送通知
            if (noticeModel != null && StringUtils.isNotEmpty(openId))
            {
                //获取token
                String accessToken = publiceService.getWeiXinToken(storeId);
                logger.error("accessToken: {}", accessToken);
                Map<String, Object> map = new HashMap<>(16);
                map.put("character_string1", new TemplateData(payNo));
                map.put("thing3", new TemplateData(title));
                map.put("amount4", new TemplateData(String.valueOf(total.divide(new BigDecimal(100)))));
                map.put("date2", new TemplateData(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS)));
                logger.error("=================微信消息推送data请求值：{}", JSON.toJSONString(map));
                String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getPay_success(), map);
                logger.error("=================微信消息推送返回值：{}", response);
            }

        } catch (Exception e)
        {
            logger.error("微信回调失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信回调失败", e.getMessage());
        }
        return resultMap;
    }

    @Override
    //该方法wx提现错误须进行管理后台消息提醒不开启事务
//    @Transactional(rollbackFor = Exception.class)
    public boolean MerchantTransfersToChange(String paramJson)
    {
        try
        {
            JSONObject jsonObject = JSON.parseObject(paramJson);
            Integer    storeId    = jsonObject.getInteger("storeId");
            String     withdrawId = jsonObject.getString("withdrawId");
            //插件提现
            String pluginStatus = jsonObject.getString("pluginStatus");
            if (StringUtils.isNotEmpty(pluginStatus))
            {
                return MerchantTransfersToChange(storeId, withdrawId, withdrawId);
            }
            WithdrawModel withdrawModel = withdrawModelMapper.selectByPrimaryKey(withdrawId);
            if (withdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJLBCZ, "提现记录不存在");
            }
            if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.YHK))
            {
                logger.error("【微信零钱提现】提现申请id{}，为银行卡提现", withdrawId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //获取用户信息
            User user = userBaseMapper.selectByUserId(storeId, withdrawModel.getUser_id());
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, "wechat_v3_withdraw");
            if (StringUtils.isEmpty(paymentJson))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXYRTXWPZ);
            }
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            //微信SDK提供
            TransferBatchService service =
                    new TransferBatchService.Builder().config(merchantConfig.getRSAConfig(storeId)).build();
            InitiateBatchTransferRequest request     = new InitiateBatchTransferRequest();
            String                       batchName   = "";
            String                       BatchRemark = "";
            String                       OutBatchNo  = "";
            String                       OutDetailNo = "";
            //店铺提现
            if (withdrawModel.getIs_mch() == 1)
            {
                batchName = "店铺余额提现";
                BatchRemark = withdrawModel.getName() + "用户进行" + batchName;
            }
            else
            {
                //用户提现
                batchName = "用户余额提现";
                BatchRemark = withdrawModel.getName() + "用户进行" + batchName;
            }
            //微信金额单位分处理
            BigDecimal yb          = new BigDecimal("100");
            BigDecimal totalAmount = withdrawModel.getMoney().subtract(withdrawModel.getS_charge());
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                totalAmount = totalAmount.multiply(yb);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //构造请求微信支付发起转账的参数
            //【批次名称】 该笔批量转账的名称
            request.setBatchName(batchName);
            //【商户appid】 申请商户号的appid或商户号绑定的appid（企业号corpid即为此appid）
            request.setAppid(payJson.getString("appid"));
            //【商家批次单号】 商户系统内部的商家批次单号，要求此参数只能由数字、大小写字母组成，在商户系统内部唯一
            request.setOutBatchNo(withdrawModel.getTxsno());
            //【批次备注】 转账说明，UTF8编码，最多允许32个字符
            request.setBatchRemark(BatchRemark);
            //【转账总金额】 转账金额单位为“分”。转账总金额必须与批次内所有明细转账金额之和保持一致，否则无法发起转账操作
            request.setTotalAmount(totalAmount.longValue());
            //【转账总笔数】 一个转账批次单最多发起一千笔转账。转账总笔数必须与批次内所有明细之和保持一致，否则无法发起转账操作
            request.setTotalNum(1);


            List<TransferDetailInput> transferDetailInputs = new ArrayList<>();
            TransferDetailInput       transferDetailInput  = new TransferDetailInput();
            //【收款用户openid】 商户appid下，某用户的openid
            transferDetailInput.setOpenid(user.getWx_id());
            //【商家明细单号】 商户系统内部区分转账批次单下不同转账明细单的唯一标识，要求此参数只能由数字、大小写字母组成
            transferDetailInput.setOutDetailNo(withdrawModel.getTxsno() + withdrawId);
            //【转账金额】 转账金额单位为“分”
            transferDetailInput.setTransferAmount(totalAmount.longValue());
            //【转账备注】 单条转账备注（微信用户会收到该备注），UTF8编码，最多允许32个字符
            transferDetailInput.setTransferRemark(BatchRemark);
            if (withdrawModel.getMoney().compareTo(new BigDecimal(2000)) >= 0
                    && StringUtils.isNotEmpty(withdrawModel.getRealname()))
            {
                transferDetailInput.setUserName(withdrawModel.getRealname());
            }

            transferDetailInputs.add(transferDetailInput);
            //【转账明细列表】 发起批量转账的明细列表，最多一千笔
            request.setTransferDetailList(transferDetailInputs);
            try
            {
                InitiateBatchTransferResponse response = service.initiateBatchTransfer(request);
                //【微信批次单号】 微信批次单号，微信商家转账系统返回的唯一标识
                String batchId = response.getBatchId();
                withdrawModel.setWxOpenId(user.getWx_id());
                withdrawModel.setWxName(user.getWx_name());
                withdrawModel.setWxSon(batchId);
                withdrawModel.setWxStatus(WithdrawModel.WX_STATUS.SUCCESS);
                withdrawModelMapper.updateByPrimaryKeySelective(withdrawModel);
            }
            catch (ServiceException e)
            {
                // http请求成功，但是wx接口失败，这里需要根据实际需求处理错误码
                logger.error("商家转账到零钱错误!");
                logger.error("错误信息:" + e.getErrorCode() + "," + e.getErrorMessage());
                //管理后台消息通知
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(storeId);
                messageLoggingSave.setMch_id(user.getMchId());
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setAdd_date(new Date());
                if (e.getErrorCode().equals("403"))
                {
                    //商户账户资金不足，请充值后原单重试，请勿更换商家转账批次单号
                    if (withdrawModel.getIs_mch() == 1)
                    {
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
                        messageLoggingSave.setParameter(mchModel.getName());
                        messageLoggingSave.setContent(String.format("ID为%s的店铺申请提现打款余额不足，请及时充值！", mchModel.getId()));
                    }
                    else
                    {
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                        messageLoggingSave.setParameter(user.getUser_name());
                        messageLoggingSave.setContent(String.format("ID为%s的用户申请提现打款余额不足，请及时充值！", user.getUser_id()));
                    }
                }
                else
                {
                    if (withdrawModel.getIs_mch() == 1)
                    {
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
                        messageLoggingSave.setParameter(mchModel.getName());
                        messageLoggingSave.setContent(String.format("ID为%s的店铺申请提现打款失败，失败原因:%s", mchModel.getId(), e.getErrorMessage()));
                    }
                    else
                    {
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                        messageLoggingSave.setParameter(user.getUser_name());
                        messageLoggingSave.setContent(String.format("ID为%s的用户申请提现打款失败，失败原因:%s", user.getUser_id(), e.getErrorMessage()));
                    }
                }
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                throw new LaiKeAPIException(e.getErrorCode(), e.getErrorMessage());
            }
            catch (LaiKeAPIException l)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("商家转账到零钱异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商家转账到零钱异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void QueryBatchTransferOrder(MainVo vo)
    {
        try
        {
            int storeId = vo.getStoreId();
            //获取商城商家转账到零钱正在进行中的订单
            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setStore_id(storeId);
            withdrawModel.setWithdrawStatus(WithdrawModel.WITHDRAW_STATUS.WX);
            withdrawModel.setWxStatus(WithdrawModel.WX_STATUS.WAIT_PAY);
            List<WithdrawModel> list = withdrawModelMapper.select(withdrawModel);
            //已完成
            int successNum = 0;
            //失败
            int failNum = 0;
            if (list.size() > 0)
            {
                logger.info("商城id: {}, 正在进行中的商城商家转账到零钱订单数量{}", storeId, list.size());
                //微信SDK提供
                TransferBatchService service =
                        new TransferBatchService.Builder().config(merchantConfig.getRSAConfig(storeId)).build();
                GetTransferBatchByOutNoRequest request = new GetTransferBatchByOutNoRequest();
                TransferBatchEntity            response;
                RecordDetailsModel             newRecordDetailsModel;
                for (WithdrawModel model : list)
                {
                    request.setOutBatchNo(model.getTxsno());
                    request.setNeedQueryDetail(Boolean.FALSE);
                    try
                    {
                        response = service.getTransferBatchByOutNo(request);
                    }
                    catch (ServiceException e)
                    {
                        // http请求成功，但是接口失败，这里需要根据实际需求处理错误码
                        logger.error("商家转账到零钱查询批次转账单结果错误!商家订单号:" + model.getTxsno());
                        logger.error("错误信息:" + e.getErrorCode() + "," + e.getErrorMessage());
                        throw new LaiKeAPIException(e.getErrorCode(), e.getErrorMessage());
                    }
                    catch (LaiKeAPIException e)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "QueryBatchTransferOrder");
                    }
                    if (response.getTransferBatch().getBatchStatus().equals(WithdrawModel.BATCH_STATUS.FINISHED))
                    {
                        // 已完成。批次内的所有转账明细单都已处理完成
                        successNum++;
                        //修改提现状态和消息通知状态
                        model.setWxStatus(WithdrawModel.WX_STATUS.SUCCESS);
                    }
                    else if (response.getTransferBatch().getBatchStatus().equals(WithdrawModel.BATCH_STATUS.CLOSED))
                    {
                        // 已关闭。可查询具体的批次关闭原因确认
                        failNum++;
                        logger.error("商家转账到零钱失败!商家订单号:" + model.getTxsno());
                        logger.error("商家转账到零钱失败!错误原因:" + response.getTransferBatch().getCloseReason());
                        logger.info("商家转账到零钱失败,开始退还零钱");
                        //将提现金额退还
                        model.setWxStatus(WithdrawModel.WX_STATUS.FAIL);
                        model.setRefuse("微信余额打款失败,余额退回");
                        User user = userBaseMapper.selectByUserId(storeId, model.getUser_id());
                        if (user == null)
                        {
                            logger.error("微信余额打款失败,余额退回{}用户不存在", model.getUser_id());
                            continue;
                        }
                        if (model.getIs_mch() == 1)
                        {
                            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
                            //退还到钱包
                            int count = mchModelMapper.refuseWithdraw(mchModel.getId(), model.getMoney());
                            if (count < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                            }
                        }
                        else
                        {
                            //退还至余额
                            int count = userBaseMapper.rechargeUserPrice(user.getId(), model.getMoney().abs());
                            if (count < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFMQSHZS, "服务器繁忙,请稍后重试");
                            }
                            user = userBaseMapper.selectByPrimaryKey(user.getId());
                            //原来的提现记录
                            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                            recordDetailsModel.setStore_id(vo.getStoreId());
                            recordDetailsModel.setsNo(model.getTxsno());
                            recordDetailsModel = recordDetailsModelMapper.selectOne(recordDetailsModel);
                            //余额退还记录
                            newRecordDetailsModel = new RecordDetailsModel();
                            newRecordDetailsModel.setStore_id(vo.getStoreId());
                            newRecordDetailsModel.setMoney(model.getMoney().abs());
                            newRecordDetailsModel.setUserMoney(user.getMoney());
                            newRecordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                            newRecordDetailsModel.setType(RecordDetailsModel.type.WITHDRAWAL_OF_BALANCE);
                            newRecordDetailsModel.setsNo(recordDetailsModel.getsNo());
                            newRecordDetailsModel.setWithdrawalFees(recordDetailsModel.getWithdrawalFees());
                            newRecordDetailsModel.setWithdrawalMethod(recordDetailsModel.getWithdrawalMethod());
                            newRecordDetailsModel.setRecordTime(new Date());
                            newRecordDetailsModel.setAddTime(new Date());
                            recordDetailsModelMapper.insert(newRecordDetailsModel);

                            RecordModel recordModel = new RecordModel();
                            recordModel.setStore_id(vo.getStoreId());
                            recordModel.setUser_id(model.getUser_id());
                            recordModel.setMoney(model.getMoney());
                            recordModel.setOldmoney(user.getMoney());
                            recordModel.setEvent(String.format("%s微信提现%s失败", user.getUser_id(), model.getMoney()));
                            recordModel.setType(22);
                            recordModel.setDetails_id(newRecordDetailsModel.getId());
                            recordModel.setAdd_date(new Date());
                            recordModelMapper.insertSelective(recordModel);
                        }
                    }
                    withdrawModelMapper.updateByPrimaryKeySelective(model);
                }

                logger.info("商城id: {}, 商城商家转账到零钱已完成订单数量{}", storeId, successNum);
                logger.info("商城id: {}, 商城商家转账到零钱失败订单数量{}", storeId, failNum);
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("商家转账到零钱查询批次转账单结果", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商家转账到零钱查询批次转账单结果 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "QueryBatchTransferOrder");
        }
    }

    /**
     * 验证微信签名
     *
     * @param request
     * @param notifyjson
     * @param apiV3Key
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    private boolean verifiedSign(HttpServletRequest request, String notifyjson, String apiV3Key) throws GeneralSecurityException, IOException, InstantiationException, IllegalAccessException, ParseException
    {
        /**微信返回的证书序列号*/
        String serialNo = request.getHeader("Wechatpay-Serial");
        //微信返回的随机字符串
        String nonceStr = request.getHeader("Wechatpay-Nonce");
        //微信返回的时间戳
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        //微信返回的签名
        String wechatSign = request.getHeader("Wechatpay-Signature");
        //组装签名字符串
        String signStr = Stream.of(timestamp, nonceStr, notifyjson).collect(Collectors.joining("\n", "", "\n"));
        try
        {

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean MerchantTransfersToChange(Integer storeId, String withdrawId, String pluginStatus)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //FX
            DistributionWithdrawModel distributionWithdrawModel = null;
            String                    userId                    = "";
            BigDecimal                Money                     = BigDecimal.ZERO;
            BigDecimal                s_charge                  = BigDecimal.ZERO;
            String                    OutBatchNo                = "";
            String                    OutDetailNo               = "";
            String                    realName                  = "";
            if (pluginStatus.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
            {
                distributionWithdrawModel = distributionWithdrawModelMapper.selectByPrimaryKey(withdrawId);
                if (distributionWithdrawModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJLBCZ, "提现记录不存在");
                }
                if (distributionWithdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.YHK))
                {
                    logger.error("【微信零钱提现】提现申请id{}，为银行卡提现", withdrawId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                userId = distributionWithdrawModel.getUser_id();
                Money = distributionWithdrawModel.getMoney();
                s_charge = distributionWithdrawModel.getS_charge();
                OutBatchNo = BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 12);
                OutDetailNo = BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10);
                realName = distributionWithdrawModel.getRealname();
            }
            //获取用户信息
            User user = userBaseMapper.selectByUserId(storeId, userId);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, "wechat_v3_withdraw");
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            //微信SDK提供
            TransferBatchService service =
                    new TransferBatchService.Builder().config(merchantConfig.getRSAConfig(storeId)).build();
            InitiateBatchTransferRequest request     = new InitiateBatchTransferRequest();
            String                       batchName   = "";
            String                       BatchRemark = "";
            //用户提现
            batchName = "用户余额提现";
            BatchRemark = user.getUser_name() + "用户进行" + batchName;
            //微信金额单位分处理
            BigDecimal yb          = new BigDecimal("100");
            BigDecimal totalAmount = Money.subtract(s_charge);
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                totalAmount = totalAmount.multiply(yb);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //构造请求微信支付发起转账的参数
            //【批次名称】 该笔批量转账的名称
            request.setBatchName(batchName);
            //【商户appid】 申请商户号的appid或商户号绑定的appid（企业号corpid即为此appid）
            request.setAppid(payJson.getString("appid"));
            //【商家批次单号】 商户系统内部的商家批次单号，要求此参数只能由数字、大小写字母组成，在商户系统内部唯一
            request.setOutBatchNo(OutBatchNo);
            //【批次备注】 转账说明，UTF8编码，最多允许32个字符
            request.setBatchRemark(BatchRemark);
            //【转账总金额】 转账金额单位为“分”。转账总金额必须与批次内所有明细转账金额之和保持一致，否则无法发起转账操作
            request.setTotalAmount(totalAmount.longValue());
            //【转账总笔数】 一个转账批次单最多发起一千笔转账。转账总笔数必须与批次内所有明细之和保持一致，否则无法发起转账操作
            request.setTotalNum(1);


            List<TransferDetailInput> transferDetailInputs = new ArrayList<>();
            TransferDetailInput       transferDetailInput  = new TransferDetailInput();
            //【收款用户openid】 商户appid下，某用户的openid
            transferDetailInput.setOpenid(user.getWx_id());
            //【商家明细单号】 商户系统内部区分转账批次单下不同转账明细单的唯一标识，要求此参数只能由数字、大小写字母组成
            transferDetailInput.setOutDetailNo(OutDetailNo);
            //【转账金额】 转账金额单位为“分”
            transferDetailInput.setTransferAmount(totalAmount.longValue());
            //【转账备注】 单条转账备注（微信用户会收到该备注），UTF8编码，最多允许32个字符
            transferDetailInput.setTransferRemark(BatchRemark);
            if (Money.compareTo(new BigDecimal(2000)) >= 0
                    && StringUtils.isNotEmpty(realName))
            {
                transferDetailInput.setUserName(realName);
            }

            transferDetailInputs.add(transferDetailInput);
            //【转账明细列表】 发起批量转账的明细列表，最多一千笔
            request.setTransferDetailList(transferDetailInputs);
            try
            {
                InitiateBatchTransferResponse response = service.initiateBatchTransfer(request);
                //【微信批次单号】 微信批次单号，微信商家转账系统返回的唯一标识
                String batchId = response.getBatchId();
                if (pluginStatus.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                {
                    distributionWithdrawModel.setWxOpenId(user.getWx_id());
                    distributionWithdrawModel.setWxName(user.getWx_name());
                    distributionWithdrawModel.setWxSon(batchId);
                    distributionWithdrawModel.setWxStatus(WithdrawModel.WX_STATUS.SUCCESS);
                    distributionWithdrawModelMapper.updateByPrimaryKeySelective(distributionWithdrawModel);
                }
            }
            catch (ServiceException e)
            {
                // http请求成功，但是wx接口失败，这里需要根据实际需求处理错误码
                logger.error("商家转账到零钱错误!");
                logger.error("错误信息:" + e.getErrorCode() + "," + e.getErrorMessage());
                //管理后台消息通知
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(storeId);
                messageLoggingSave.setMch_id(user.getMchId());
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setAdd_date(new Date());
                if (e.getErrorCode().equals("403"))
                {
                    //商户账户资金不足，请充值后原单重试，请勿更换商家转账批次单号
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                    messageLoggingSave.setParameter(user.getUser_name());
                    messageLoggingSave.setContent(String.format("ID为%s的用户申请提现打款余额不足，请及时充值！", user.getUser_id()));
                }
                else
                {
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                    messageLoggingSave.setParameter(user.getUser_name());
                    messageLoggingSave.setContent(String.format("ID为%s的用户申请提现打款失败，失败原因:%s", user.getUser_id(), e.getErrorMessage()));
                }
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                throw new LaiKeAPIException(e.getErrorCode(), e.getErrorMessage());
            }
            catch (LaiKeAPIException l)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("商家转账到零钱异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商家转账到零钱异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
        }
        return true;
    }

}
